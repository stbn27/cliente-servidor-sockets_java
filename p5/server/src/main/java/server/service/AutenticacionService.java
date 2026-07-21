package server.service;

import at.favre.lib.crypto.bcrypt.BCrypt;
import common.exception.AutenticacionException;
import common.exception.ServicioNoDisponibleException;
import common.exception.ValidacionException;
import common.model.Sesion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import server.database.DatabaseManager;
import server.repository.AutorEntity;
import server.repository.AutorRepository;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Optional;

/**
 * Servicio de autenticación de usuarios. Válida credenciales contra la base
 * de datos y crea una sesión activa si la autenticación es exitosa.
 * <p>
 * Las contraseñas se reciben como {@code char[]} y se limpian de memoria
 * ({@code Arrays.fill}) al finalizar, independientemente del resultado.
 */
public final class AutenticacionService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AutenticacionService.class);
    private static final int USUARIO_MAXIMO = 50;     // Longitud máxima del nombre de usuario
    private static final int CONTRASENA_MAXIMA = 256; // Longitud máxima de la contraseña

    private final DatabaseManager database;     // Gestor de conexiones a la BD
    private final AutorRepository autores;       // Repositorio para consultar autores
    private final SesionService sesiones;       // Servicio para crear sesiones

    /**
     * Construye el servicio de autenticación con sus tres dependencias.
     *
     * @param database gestor de conexiones a la base de datos
     * @param autores  repositorio para buscar autores por usuario
     * @param sesiones servicio encargado de crear sesiones tras login exitoso
     */
    public AutenticacionService(DatabaseManager database, AutorRepository autores,
                                SesionService sesiones) {
        this.database = database;       // Guarda referencia al gestor de BD
        this.autores = autores;         // Guarda referencia al repositorio
        this.sesiones = sesiones;       // Guarda referencia al servicio de sesiones
    }

    /**
     * Autentica a un usuario Válidando su nombre de usuario y contraseña contra
     * la base de datos. Si la autenticación es exitosa, crea y devuelve una
     * sesión activa. Si el usuario no existe, está inactivo o la contraseña
     * no coincide, lanza {@link AutenticacionException} sin revelar cuál fue
     * el motivo específico (medida de seguridad anti-enumeración).
     * <p>
     * La contraseña se limpia de memoria al finalizar, sin importar si la
     * autenticación tuvo éxito o no.
     *
     * @param usuario   nombre de usuario a autenticar
     * @param contrasena contraseña en texto plano como arreglo de caracteres
     * @return una sesión activa si la autenticación fue exitosa
     * @throws ValidacionException           si el usuario o la contraseña no
     *                                        cumplen las Válidaciones de formato
     * @throws AutenticacionException         si las credenciales son incorrectas
     *                                        o el usuario está inactivo
     * @throws ServicioNoDisponibleException si ocurre un error de base de datos
     */
    public Sesion iniciar(String usuario, char[] contrasena)
            throws ValidacionException, AutenticacionException,
            ServicioNoDisponibleException {
        try {
            // Válida y normaliza el nombre de usuario (trim, no vacío, max 50)
            String usuarioNormalizado = ValidarUsuario(usuario);

            // Válida que la contraseña no sea null, vacía ni mayor a 256 caracteres
            ValidarContrasena(contrasena);

            try (Connection connection = database.getConnection()) {
                // Busca el autor por su nombre de usuario en la BD
                Optional<AutorEntity> encontrado =
                        autores.buscarPorUsuario(connection, usuarioNormalizado);

                // Verifica: existe, está activo, y la contraseña coincide
                // NOTA: Si no se hasheo la contraseña en DatabaseInitializer,
                // cambia `!BCrypt.verifyer().verify(contrasena, encontrado.get().passwordHash()).verified` por:
                //    !new String(contrasena).equals(encontrado.get().passwordHash())
                if (encontrado.isEmpty()
                        || !encontrado.get().activo()
                        || !BCrypt.verifyer().verify(contrasena, encontrado.get().passwordHash()).verified
                ) {
                    // Registra el intento fallido (sin revelar el motivo específico)
                    LOGGER.warn("Inicio de sesión rechazado para usuario {}", usuarioNormalizado);

                    // Lanza error genérico (anti-enumeración: no dice si fue usuario o contraseña)
                    throw new AutenticacionException("Usuario o contraseña incorrectos.");
                }

                // Login exitoso: registra en log y crea la sesión
                LOGGER.info("Inicio de sesión exitoso para {}", usuarioNormalizado);
                return sesiones.crear(encontrado.get());

            } catch (SQLException error) {
                // Error de BD: lo loguea y lanza excepción de servicio no disponible
                LOGGER.error("Fallo de base de datos al autenticar", error);
                throw new ServicioNoDisponibleException("No fue posible autenticar en este momento.");
            }
        } finally {
            // Limpia la contraseña de memoria por seguridad, sin importar el resultado
            if (contrasena != null) {
                Arrays.fill(contrasena, '\0'); // Sobrescribe el array con caracteres nulos
            }
        }
    }

    /**
     * Válida y normaliza el nombre de usuario. Elimina espacios al inicio y
     * final ({@code strip}), verifica que no esté vacío y que no exceda los
     * 50 caracteres.
     *
     * @param usuario nombre de usuario en crudo (puede ser null)
     * @return el nombre de usuario normalizado (sin espacios sobrantes)
     * @throws ValidacionException si el usuario es null, está vacío tras
     *                             normalizar, o supera los 50 caracteres
     */
    private String ValidarUsuario(String usuario) throws ValidacionException {
        if (usuario == null) {
            throw new ValidacionException("El usuario es obligatorio."); // Null directo: error
        }

        String normalizado = usuario.strip(); // Quita espacios al inicio y final

        if (normalizado.isEmpty() || normalizado.length() > USUARIO_MAXIMO) {
            throw new ValidacionException(
                    "El usuario es obligatorio y no puede superar 50 caracteres.");
        }

        return normalizado; // Retorna el usuario ya limpio
    }

    /**
     * Válida que la contraseña cumpla con las restricciones de longitud:
     * no ser null, no estar vacía y no superar los 256 caracteres.
     *
     * @param contrasena contraseña como arreglo de caracteres
     * @throws ValidacionException si la contraseña es null, vacía o
     *                             supera los 256 caracteres
     */
    private void ValidarContrasena(char[] contrasena) throws ValidacionException {
        if (contrasena == null || contrasena.length == 0
                || contrasena.length > CONTRASENA_MAXIMA) {
            throw new ValidacionException(
                    "La contraseña debe contener entre 1 y 256 caracteres.");
        }
    }
}