package servidor.excepciones;

/**
 * Error asociado con lectura o escritura de archivos CSV.
 */
public final class ErrorPersistencia extends ErrorAplicacion {

    /**
     * Crea un error de persistencia.
     *
     * @param mensaje mensaje legible.
     * @param detalleTecnico detalle controlado del problema.
     * @param modulo modulo afectado.
     */
    public ErrorPersistencia(String mensaje, String detalleTecnico, String modulo) {
        super(CodigosError.ERROR_PERSISTENCIA, mensaje, detalleTecnico, modulo);
    }
}
