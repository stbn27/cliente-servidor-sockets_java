package server;

import common.exception.AutenticacionException;
import common.exception.AutorizacionException;
import common.model.Categoria;
import common.model.NuevaNoticia;
import common.model.Noticia;
import common.model.Rol;
import common.model.Sesion;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import server.database.DatabaseInitializer;
import server.database.DatabaseManager;
import server.repository.AutorEntity;
import server.repository.AutorRepository;
import server.repository.NoticiaRepository;
import server.service.AutenticacionService;
import server.service.NoticiaService;
import server.service.SesionService;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Casos explícitos que complementan los flujos integrales y de RMI. */
class RequirementsCoverageTest {
    private DatabaseManager database;
    private DatabaseInitializer initializer;
    private SesionService sesiones;
    private AutenticacionService autenticacion;
    private NoticiaService noticias;

    @BeforeEach
    void preparar() throws Exception {
        database = DatabaseManager.inMemory("requirements_" + System.nanoTime());
        initializer = new DatabaseInitializer(database);
        initializer.inicializar();
        sesiones = new SesionService(Duration.ofMinutes(30));
        autenticacion = new AutenticacionService(database, new AutorRepository(), sesiones);
        noticias = new NoticiaService(database, new NoticiaRepository(), sesiones);
    }

    @AfterEach
    void cerrar() {
        database.close();
    }

    @Test
    void inicializacionEsIdempotenteYLasContrasenasNoEstanEnTextoPlano() throws Exception {
        initializer.inicializar();

        try (Connection connection = database.getConnection();
             PreparedStatement autores = connection.prepareStatement(
                     "SELECT usuario,password_hash FROM autores ORDER BY usuario");
             ResultSet rows = autores.executeQuery()) {
            int cantidad = 0;
            while (rows.next()) {
                cantidad++;
                String usuario = rows.getString("usuario");
                String hash = rows.getString("password_hash");
                assertTrue(hash.startsWith("$2"), "La semilla debe almacenar un hash BCrypt");
                assertFalse(hash.contains(usuario));
                assertFalse(hash.equals("redactor123"));
                assertFalse(hash.equals("noticias123"));
            }
            assertEquals(2, cantidad);
        }
        assertEquals(3, noticias.listar().size(), "La segunda inicialización no duplica noticias");
    }

    @Test
    void autenticacionLimpiaLaContrasenaYCerrarSesionInvalidaElToken() throws Exception {
        char[] password = "redactor123".toCharArray();
        Sesion sesion = autenticacion.iniciar("redactor1", password);

        assertTrue(Arrays.equals(new char[password.length], password));
        sesiones.cerrar(sesion.token());
        assertThrows(AutenticacionException.class,
                () -> noticias.publicar(sesion.token(),
                        new NuevaNoticia("Sin sesión", "Contenido", Categoria.GENERAL)));
    }

    @Test
    void unLectorNoPuedePublicarAunquePoseaUnaSesionValida() {
        Sesion lector = sesiones.crear(new AutorEntity(
                99, "lector", "hash-no-expuesto", "Lector", Rol.LECTOR, true));

        assertThrows(AutorizacionException.class,
                () -> noticias.publicar(lector.token(),
                        new NuevaNoticia("No autorizada", "Contenido", Categoria.GENERAL)));
    }

    @Test
    void lectoresListanYBuscanMientrasSePublicanNoticias() throws Exception {
        Sesion redactor = autenticacion.iniciar("redactor1", "redactor123".toCharArray());
        int publicaciones = 10;
        int lectores = 16;
        ExecutorService executor = Executors.newFixedThreadPool(10);
        CountDownLatch inicio = new CountDownLatch(1);
        List<Future<Noticia>> escrituras = new ArrayList<>();
        List<Future<List<Noticia>>> lecturas = new ArrayList<>();

        try {
            IntStream.range(0, publicaciones).forEach(indice -> escrituras.add(executor.submit(() -> {
                inicio.await();
                return noticias.publicar(redactor.token(), new NuevaNoticia(
                        "Publicación paralela " + indice,
                        "Contenido marcador-concurrente " + indice,
                        Categoria.TECNOLOGIA));
            })));
            IntStream.range(0, lectores).forEach(indice -> lecturas.add(executor.submit(() -> {
                inicio.await();
                return indice % 2 == 0
                        ? noticias.listar()
                        : noticias.buscar("MARCADOR-CONCURRENTE");
            })));

            inicio.countDown();
            Set<Long> ids = escrituras.stream()
                    .map(this::resultado)
                    .map(Noticia::id)
                    .collect(Collectors.toSet());
            assertEquals(publicaciones, ids.size(), "Cada publicación obtiene un ID distinto");
            lecturas.forEach(future -> assertTrue(
                    resultado(future).stream().allMatch(Objects::nonNull)));

            List<Noticia> resultadoFinal = noticias.buscar("marcador-concurrente");
            assertEquals(publicaciones, resultadoFinal.size());
            assertTrue(resultadoFinal.stream().allMatch(noticia -> ids.contains(noticia.id())));
        } finally {
            executor.shutdown();
            assertTrue(executor.awaitTermination(10, TimeUnit.SECONDS));
        }
    }

    private <T> T resultado(Future<T> future) {
        try {
            return future.get(10, TimeUnit.SECONDS);
        } catch (Exception exception) {
            throw new AssertionError("Falló una operación concurrente", exception);
        }
    }
}
