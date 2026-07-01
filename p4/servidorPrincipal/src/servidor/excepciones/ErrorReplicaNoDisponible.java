package servidor.excepciones;

/**
 * Error usado cuando la replica no responde o devuelve un fallo.
 */
public final class ErrorReplicaNoDisponible extends ErrorAplicacion {

    /**
     * Crea un error para problemas de replicacion.
     *
     * @param mensaje mensaje legible.
     * @param detalleTecnico detalle tecnico controlado.
     * @param modulo modulo de replicacion.
     */
    public ErrorReplicaNoDisponible(String mensaje, String detalleTecnico, String modulo) {
        super(CodigosError.SERVIDOR_REPLICA_NO_DISPONIBLE, mensaje, detalleTecnico, modulo);
    }
}
