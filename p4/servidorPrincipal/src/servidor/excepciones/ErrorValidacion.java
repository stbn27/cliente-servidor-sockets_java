package servidor.excepciones;

/**
 * Representa errores producidos por parametros invalidos o vacios.
 */
public final class ErrorValidacion extends ErrorAplicacion {

    /**
     * Construye un error de validacion.
     *
     * @param mensaje mensaje legible.
     * @param detalleTecnico detalle tecnico controlado.
     * @param modulo modulo donde se detecto el problema.
     */
    public ErrorValidacion(String mensaje, String detalleTecnico, String modulo) {
        super(CodigosError.PARAMETRO_INVALIDO, mensaje, detalleTecnico, modulo);
    }
}
