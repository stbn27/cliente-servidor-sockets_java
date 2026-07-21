package common.dto;

import java.io.Serializable;
import java.time.Instant;

public record EstadoServidor(boolean disponible, boolean baseDatosDisponible, String mensaje,
                             Instant instanteServidor) implements Serializable {

    private static final long serialVersionUID = 1L;

}
