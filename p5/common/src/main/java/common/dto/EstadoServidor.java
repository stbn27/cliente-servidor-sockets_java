package common.dto;

import java.io.Serializable;
import java.time.Instant;

public final class EstadoServidor implements Serializable {

    private static final long serialVersionUID = 1L;

    private final boolean disponible;
    private final boolean baseDatosDisponible;
    private final String mensaje;
    private final Instant instanteServidor;

    public EstadoServidor(boolean disponible, boolean baseDatosDisponible,
                          String mensaje, Instant instanteServidor) {
        this.disponible = disponible;
        this.baseDatosDisponible = baseDatosDisponible;
        this.mensaje = mensaje;
        this.instanteServidor = instanteServidor;
    }

    public boolean isDisponible() {
        return disponible;
    }

    public boolean isBaseDatosDisponible() {
        return baseDatosDisponible;
    }

    public String getMensaje() {
        return mensaje;
    }

    public Instant getInstanteServidor() {
        return instanteServidor;
    }
}
