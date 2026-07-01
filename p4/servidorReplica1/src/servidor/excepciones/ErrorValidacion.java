package servidor.excepciones;

/**
 * Error de datos invalidos recibido por la replica.
 */
public final class ErrorValidacion extends ErrorAplicacion {

    public ErrorValidacion(String mensaje, String detalleTecnico, String modulo) {
        super(CodigosError.PARAMETRO_INVALIDO, mensaje, detalleTecnico, modulo);
    }
}
