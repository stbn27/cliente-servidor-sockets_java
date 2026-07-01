package servidor.excepciones;

/**
 * Error al manipular archivos de la replica.
 */
public final class ErrorPersistencia extends ErrorAplicacion {

    public ErrorPersistencia(String mensaje, String detalleTecnico, String modulo) {
        super(CodigosError.ERROR_PERSISTENCIA, mensaje, detalleTecnico, modulo);
    }
}
