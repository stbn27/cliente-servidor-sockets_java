package servidor.excepciones;

/**
 * Se usa cuando un recurso buscado no existe en los datasets.
 */
public final class ErrorNoEncontrado extends ErrorAplicacion {

    /**
     * Construye un error de recurso inexistente.
     *
     * @param mensaje mensaje legible.
     * @param detalleTecnico detalle controlado.
     * @param modulo modulo que realizo la busqueda.
     */
    public ErrorNoEncontrado(String mensaje, String detalleTecnico, String modulo) {
        super(CodigosError.RECURSO_NO_ENCONTRADO, mensaje, detalleTecnico, modulo);
    }
}
