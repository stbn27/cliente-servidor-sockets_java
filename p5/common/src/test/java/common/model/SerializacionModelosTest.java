package common.model;

import common.dto.EstadoServidor;
import common.exception.ConflictoEdicionException;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class SerializacionModelosTest {

    @Test
    void noticiaConservaSusDatosAlCruzarLaFronteraRmi() throws Exception {
        LocalDateTime creada = LocalDateTime.of(2026, 7, 20, 10, 30);
        Noticia original = new Noticia(7, "RMI", "Contenido", Categoria.TECNOLOGIA,
                2, "Ada", creada, creada.plusMinutes(5), 3);

        Noticia copia = copiar(original);

        assertEquals(original.getId(), copia.getId());
        assertEquals(original.getTitulo(), copia.getTitulo());
        assertEquals(original.getContenido(), copia.getContenido());
        assertEquals(original.getCategoria(), copia.getCategoria());
        assertEquals(original.getAutorId(), copia.getAutorId());
        assertEquals(original.getAutorNombre(), copia.getAutorNombre());
        assertEquals(original.getFechaCreacion(), copia.getFechaCreacion());
        assertEquals(original.getFechaModificacion(), copia.getFechaModificacion());
        assertEquals(original.getVersion(), copia.getVersion());
    }

    @Test
    void sesionYEstadoSonSerializables() throws Exception {
        Instant expiracion = Instant.parse("2026-07-20T18:00:00Z");
        Sesion sesion = copiar(new Sesion("token", 5, "redactor1", "Ada",
                Rol.REDACTOR, expiracion));
        EstadoServidor estado = copiar(new EstadoServidor(true, true, "Disponible", expiracion));

        assertEquals("token", sesion.getToken());
        assertEquals("Ada", sesion.getAutorNombre());
        assertEquals(expiracion, sesion.getExpiraEn());
        assertEquals(expiracion, estado.getInstanteServidor());
    }

    @Test
    void excepcionDeConflictoConservaVersiones() throws Exception {
        ConflictoEdicionException copia = copiar(new ConflictoEdicionException(
                "La noticia fue modificada.", 1, 2));

        assertEquals(1, copia.getVersionEsperada());
        assertEquals(2, copia.getVersionActual());
        assertInstanceOf(Serializable.class, copia);
    }

    @Test
    void autorCompartidoNoExponeHashDeContrasena() {
        assertFalse(java.util.Arrays.stream(Autor.class.getDeclaredFields())
                .anyMatch(campo -> campo.getName().toLowerCase().contains("password")
                        || campo.getName().toLowerCase().contains("hash")));
    }

    @SuppressWarnings("unchecked")
    private static <T extends Serializable> T copiar(T original) throws Exception {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (ObjectOutputStream salida = new ObjectOutputStream(bytes)) {
            salida.writeObject(original);
        }
        try (ObjectInputStream entrada = new ObjectInputStream(
                new ByteArrayInputStream(bytes.toByteArray()))) {
            return (T) entrada.readObject();
        }
    }
}
