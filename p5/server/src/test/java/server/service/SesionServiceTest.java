package server.service;

import common.exception.AutenticacionException;
import common.model.Rol;
import org.junit.jupiter.api.Test;
import server.repository.AutorEntity;

import java.security.SecureRandom;
import java.time.*;

import static org.junit.jupiter.api.Assertions.*;

class SesionServiceTest {
    @Test void rechazaTokenInvalidoYExpirado() throws Exception {
        MutableClock clock = new MutableClock(Instant.parse("2026-01-01T00:00:00Z"));
        SesionService service = new SesionService(Duration.ofMinutes(1), clock, new SecureRandom());
        assertThrows(AutenticacionException.class, () -> service.requerir("inexistente"));
        var session = service.crear(new AutorEntity(1,"autor","hash","Autor", Rol.REDACTOR,true));
        assertEquals(1, service.requerir(session.getToken()).autorId());
        clock.instant = clock.instant.plusSeconds(61);
        assertThrows(AutenticacionException.class, () -> service.requerir(session.getToken()));
    }

    private static final class MutableClock extends Clock {
        private Instant instant; MutableClock(Instant instant){this.instant=instant;}
        public ZoneId getZone(){return ZoneOffset.UTC;} public Clock withZone(ZoneId zone){return this;} public Instant instant(){return instant;}
    }
}
