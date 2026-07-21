package server.service;

import common.exception.AutenticacionException;
import common.exception.AutorizacionException;
import common.model.Rol;
import common.model.Sesion;
import server.repository.AutorEntity;

import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;
import java.util.Base64;
import java.util.concurrent.ConcurrentHashMap;

public final class SesionService {
    private static final int TOKEN_BYTES = 32;

    private final ConcurrentHashMap<String, SesionServidor> sesiones =
            new ConcurrentHashMap<>();
    private final Duration timeout;
    private final Clock clock;
    private final SecureRandom random;

    public SesionService(Duration timeout) {
        this(timeout, Clock.systemUTC(), new SecureRandom());
    }

    public SesionService(Duration timeout, Clock clock, SecureRandom random) {
        this.timeout = timeout;
        this.clock = clock;
        this.random = random;
    }

    public Sesion crear(AutorEntity autor) {
        byte[] bytes = new byte[TOKEN_BYTES];
        random.nextBytes(bytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        SesionServidor sesion = new SesionServidor(
                token, autor.id(), autor.usuario(), autor.nombre(), autor.rol(),
                clock.instant().plus(timeout));
        sesiones.put(token, sesion);
        return sesion.publica();
    }

    public SesionServidor requerir(String token) throws AutenticacionException {
        if (token == null || token.isBlank()) {
            throw sesionInvalida();
        }
        SesionServidor sesion = sesiones.get(token);
        if (sesion == null) {
            throw sesionInvalida();
        }
        if (!clock.instant().isBefore(sesion.expiraEn())) {
            sesiones.remove(token, sesion);
            throw new AutenticacionException(
                    "La sesión expiró. Inicie sesión nuevamente.");
        }
        return sesion;
    }

    public SesionServidor requerirRedactor(String token)
            throws AutenticacionException, AutorizacionException {
        SesionServidor sesion = requerir(token);
        if (sesion.rol() != Rol.REDACTOR) {
            throw new AutorizacionException(
                    "La operación requiere el rol de redactor.");
        }
        return sesion;
    }

    public void cerrar(String token) {
        if (token != null) {
            sesiones.remove(token);
        }
    }

    private AutenticacionException sesionInvalida() {
        return new AutenticacionException(
                "La sesión no es válida. Inicie sesión nuevamente.");
    }
}
