package common.model;

import java.io.Serializable;
import java.time.Instant;

public record Sesion(String token, long autorId, String usuario, String autorNombre, Rol rol,
                     Instant expiraEn) implements Serializable {

    private static final long serialVersionUID = 1L;

}
