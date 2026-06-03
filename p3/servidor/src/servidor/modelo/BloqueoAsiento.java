package servidor.modelo;

import java.time.LocalDateTime;

/**
 * Representa el bloqueo temporal de un asiento para una sesion especifica.
 */
public class BloqueoAsiento {
    private final String idFuncion;
    private final String numeroAsiento;
    private final String idCliente;
    private final String nombreCliente;
    private final LocalDateTime fechaHoraInicio;
    private final LocalDateTime fechaHoraExpiracion;

    /**
     * Crea un nuevo bloqueo temporal.
     *
     * @param idFuncion identificador de la funcion.
     * @param numeroAsiento numero del asiento bloqueado.
     * @param idCliente identificador interno de la sesion.
     * @param nombreCliente nombre del cliente.
     * @param fechaHoraInicio fecha y hora del bloqueo.
     * @param fechaHoraExpiracion fecha y hora de expiracion del bloqueo.
     */
    public BloqueoAsiento(String idFuncion, String numeroAsiento, String idCliente, String nombreCliente,
            LocalDateTime fechaHoraInicio, LocalDateTime fechaHoraExpiracion) {
        this.idFuncion = idFuncion;
        this.numeroAsiento = numeroAsiento;
        this.idCliente = idCliente;
        this.nombreCliente = nombreCliente;
        this.fechaHoraInicio = fechaHoraInicio;
        this.fechaHoraExpiracion = fechaHoraExpiracion;
    }

    /**
     * Obtiene el identificador de la funcion.
     *
     * @return identificador de la funcion.
     */
    public String getIdFuncion() {
        return idFuncion;
    }

    /**
     * Obtiene el numero del asiento.
     *
     * @return numero del asiento bloqueado.
     */
    public String getNumeroAsiento() {
        return numeroAsiento;
    }

    /**
     * Obtiene el identificador interno de la sesion.
     *
     * @return identificador del cliente.
     */
    public String getIdCliente() {
        return idCliente;
    }

    /**
     * Obtiene el nombre del cliente.
     *
     * @return nombre del cliente.
     */
    public String getNombreCliente() {
        return nombreCliente;
    }

    /**
     * Obtiene la fecha y hora en que inicio el bloqueo.
     *
     * @return fecha y hora de inicio.
     */
    public LocalDateTime getFechaHoraInicio() {
        return fechaHoraInicio;
    }

    /**
     * Obtiene la fecha y hora exacta de expiracion.
     *
     * @return fecha y hora de expiracion.
     */
    public LocalDateTime getFechaHoraExpiracion() {
        return fechaHoraExpiracion;
    }

    /**
     * Indica si el bloqueo ya expiro en la referencia indicada.
     *
     * @param referencia fecha y hora a comparar.
     * @return {@code true} si el bloqueo ya expiro.
     */
    public boolean haExpirado(LocalDateTime referencia) {
        return !fechaHoraExpiracion.isAfter(referencia);
    }
}
