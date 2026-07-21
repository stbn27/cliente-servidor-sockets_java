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

public final class AutenticacionService {
    private static final Logger LOGGER = LoggerFactory.getLogger(AutenticacionService.class);
    private static final int USUARIO_MAXIMO = 50;
    private static final int CONTRASENA_MAXIMA = 256;

    private final DatabaseManager database;
    private final AutorRepository autores;
    private final SesionService sesiones;

    public AutenticacionService(DatabaseManager database, AutorRepository autores,
                                SesionService sesiones) {
        this.database = database;
        this.autores = autores;
        this.sesiones = sesiones;
    }

    public Sesion iniciar(String usuario, char[] contrasena)
            throws ValidacionException, AutenticacionException,
            ServicioNoDisponibleException {
        try {
            String usuarioNormalizado = validarUsuario(usuario);
            validarContrasena(contrasena);
            try (Connection connection = database.getConnection()) {
                Optional<AutorEntity> encontrado =
                        autores.buscarPorUsuario(connection, usuarioNormalizado);
                if (encontrado.isEmpty() || !encontrado.get().activo()
                        || !BCrypt.verifyer().verify(
                                contrasena, encontrado.get().passwordHash()).verified) {
                    LOGGER.warn("Inicio de sesión rechazado para usuario {}",
                            usuarioNormalizado);
                    throw new AutenticacionException("Usuario o contraseña incorrectos.");
                }
                LOGGER.info("Inicio de sesión exitoso para {}", usuarioNormalizado);
                return sesiones.crear(encontrado.get());
            } catch (SQLException error) {
                LOGGER.error("Fallo de base de datos al autenticar", error);
                throw new ServicioNoDisponibleException(
                        "No fue posible autenticar en este momento.");
            }
        } finally {
            if (contrasena != null) {
                Arrays.fill(contrasena, '\0');
            }
        }
    }

    private String validarUsuario(String usuario) throws ValidacionException {
        if (usuario == null) {
            throw new ValidacionException("El usuario es obligatorio.");
        }
        String normalizado = usuario.strip();
        if (normalizado.isEmpty() || normalizado.length() > USUARIO_MAXIMO) {
            throw new ValidacionException(
                    "El usuario es obligatorio y no puede superar 50 caracteres.");
        }
        return normalizado;
    }

    private void validarContrasena(char[] contrasena) throws ValidacionException {
        if (contrasena == null || contrasena.length == 0
                || contrasena.length > CONTRASENA_MAXIMA) {
            throw new ValidacionException(
                    "La contraseña debe contener entre 1 y 256 caracteres.");
        }
    }
}
