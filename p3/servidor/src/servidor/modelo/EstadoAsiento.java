package servidor.modelo;

/**
 * Define los estados posibles de un asiento.
 */
public enum EstadoAsiento {
    /** Asiento disponible para bloqueo o compra. */
    DISPONIBLE,
    /** Asiento bloqueado temporalmente por un cliente. */
    BLOQUEADO,
    /** Asiento vendido de manera definitiva. */
    VENDIDO
}

