package servidor.excepciones;

/**
 * Error cuando una clave buscada no existe en la replica.
 */
public final class ErrorNoEncontrado extends ErrorAplicacion {

    public ErrorNoEncontrado(String mensaje, String detalleTecnico, String modulo) {
        super(CodigosError.RECURSO_NO_ENCONTRADO, mensaje, detalleTecnico, modulo);
    }
}
