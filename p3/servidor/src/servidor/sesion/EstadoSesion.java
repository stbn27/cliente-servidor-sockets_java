package servidor.sesion;

/**
 * Define los estados del flujo guiado para una sesion de cliente.
 */
public enum EstadoSesion {
    /** La sesion acaba de crearse y aun no se ha mostrado una pantalla. */
    INICIO,
    /** La sesion ya recibio la pantalla de funciones disponibles. */
    FUNCIONES_MOSTRADAS,
    /** La sesion tiene una funcion seleccionada. */
    FUNCION_SELECCIONADA,
    /** La sesion tiene un asiento bloqueado temporalmente. */
    ASIENTO_BLOQUEADO,
    /** La sesion ya confirmo una compra. */
    COMPRA_CONFIRMADA
}
