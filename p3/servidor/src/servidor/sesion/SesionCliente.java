package servidor.sesion;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Mantiene el estado temporal de un cliente conectado al servidor.
 */
public class SesionCliente {
    private final String idCliente;
    private LocalDate fechaSeleccionada;
    private String idFuncionSeleccionada;
    private String asientoBloqueadoActual;
    private String nombreCliente;
    private LocalDateTime horaExpiracionBloqueo;
    private EstadoSesion estadoActual;

    /**
     * Crea una nueva sesion de cliente.
     *
     * @param idCliente identificador interno del cliente.
     */
    public SesionCliente(String idCliente) {
        this.idCliente = idCliente;
        this.estadoActual = EstadoSesion.INICIO;
    }

    /**
     * Obtiene el identificador interno del cliente.
     *
     * @return identificador interno.
     */
    public String getIdCliente() {
        return idCliente;
    }

    /**
     * Obtiene la fecha actualmente seleccionada.
     *
     * @return fecha seleccionada o {@code null}.
     */
    public LocalDate getFechaSeleccionada() {
        return fechaSeleccionada;
    }

    /**
     * Define la fecha seleccionada y ajusta el estado base del flujo.
     *
     * @param fechaSeleccionada nueva fecha seleccionada.
     */
    public void establecerFechaSeleccionada(LocalDate fechaSeleccionada) {
        this.fechaSeleccionada = fechaSeleccionada;
        if (fechaSeleccionada == null) {
            this.estadoActual = EstadoSesion.INICIO;
        } else if (idFuncionSeleccionada == null) {
            this.estadoActual = EstadoSesion.FUNCIONES_MOSTRADAS;
        }
    }

    /**
     * Obtiene el identificador de la funcion seleccionada.
     *
     * @return identificador de la funcion o {@code null}.
     */
    public String getIdFuncionSeleccionada() {
        return idFuncionSeleccionada;
    }

    /**
     * Registra la funcion seleccionada por el cliente.
     *
     * @param idFuncionSeleccionada identificador de la funcion.
     */
    public void seleccionarFuncion(String idFuncionSeleccionada) {
        this.idFuncionSeleccionada = idFuncionSeleccionada;
        this.estadoActual = EstadoSesion.FUNCION_SELECCIONADA;
    }

    /**
     * Obtiene el asiento bloqueado actualmente.
     *
     * @return asiento bloqueado o {@code null}.
     */
    public String getAsientoBloqueadoActual() {
        return asientoBloqueadoActual;
    }

    /**
     * Obtiene el nombre del cliente asociado al bloqueo actual.
     *
     * @return nombre del cliente o {@code null}.
     */
    public String getNombreCliente() {
        return nombreCliente;
    }

    /**
     * Obtiene la hora exacta de expiracion del bloqueo.
     *
     * @return hora de expiracion o {@code null}.
     */
    public LocalDateTime getHoraExpiracionBloqueo() {
        return horaExpiracionBloqueo;
    }

    /**
     * Obtiene el estado actual del flujo.
     *
     * @return estado actual de la sesion.
     */
    public EstadoSesion getEstadoActual() {
        return estadoActual;
    }

    /**
     * Registra un asiento bloqueado en la sesion.
     *
     * @param asientoBloqueadoActual asiento bloqueado.
     * @param nombreCliente nombre del cliente.
     * @param horaExpiracionBloqueo hora exacta de expiracion.
     */
    public void registrarBloqueo(String asientoBloqueadoActual, String nombreCliente,
            LocalDateTime horaExpiracionBloqueo) {
        this.asientoBloqueadoActual = asientoBloqueadoActual;
        this.nombreCliente = nombreCliente;
        this.horaExpiracionBloqueo = horaExpiracionBloqueo;
        this.estadoActual = EstadoSesion.ASIENTO_BLOQUEADO;
    }

    /**
     * Elimina la informacion del bloqueo actual y conserva la funcion.
     */
    public void limpiarBloqueo() {
        this.asientoBloqueadoActual = null;
        this.nombreCliente = null;
        this.horaExpiracionBloqueo = null;
        if (idFuncionSeleccionada != null) {
            this.estadoActual = EstadoSesion.FUNCION_SELECCIONADA;
        } else if (fechaSeleccionada != null) {
            this.estadoActual = EstadoSesion.FUNCIONES_MOSTRADAS;
        } else {
            this.estadoActual = EstadoSesion.INICIO;
        }
    }

    /**
     * Elimina la funcion seleccionada y cualquier bloqueo asociado.
     */
    public void limpiarFuncionSeleccionada() {
        this.idFuncionSeleccionada = null;
        limpiarBloqueo();
        if (fechaSeleccionada != null) {
            this.estadoActual = EstadoSesion.FUNCIONES_MOSTRADAS;
        }
    }

    /**
     * Reinicia la sesion a una fecha base y elimina toda seleccion temporal.
     *
     * @param fechaBase fecha que quedara seleccionada tras el reinicio.
     */
    public void reiniciar(LocalDate fechaBase) {
        this.fechaSeleccionada = fechaBase;
        this.idFuncionSeleccionada = null;
        this.asientoBloqueadoActual = null;
        this.nombreCliente = null;
        this.horaExpiracionBloqueo = null;
        this.estadoActual = EstadoSesion.FUNCIONES_MOSTRADAS;
    }

    /**
     * Marca la sesion como compra confirmada.
     */
    public void marcarCompraConfirmada() {
        this.estadoActual = EstadoSesion.COMPRA_CONFIRMADA;
    }

    /**
     * Indica si la sesion tiene una funcion seleccionada.
     *
     * @return {@code true} si ya existe funcion seleccionada.
     */
    public boolean tieneFuncionSeleccionada() {
        return idFuncionSeleccionada != null && !idFuncionSeleccionada.isBlank();
    }

    /**
     * Indica si la sesion tiene un asiento bloqueado.
     *
     * @return {@code true} si existe un asiento bloqueado.
     */
    public boolean tieneAsientoBloqueado() {
        return asientoBloqueadoActual != null && !asientoBloqueadoActual.isBlank();
    }
}
