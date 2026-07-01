package servidor.excepciones;

/**
 * Excepcion base para errores controlados de la replica.
 */
public class ErrorAplicacion extends Exception {

    private final String codigo;
    private final String modulo;
    private final String detalleTecnico;

    public ErrorAplicacion(String codigo, String mensaje, String detalleTecnico, String modulo) {
        super(mensaje);
        this.codigo = codigo;
        this.modulo = modulo;
        this.detalleTecnico = detalleTecnico;
    }

    public String getCodigo() {
        return codigo;
    }

    public String getModulo() {
        return modulo;
    }

    public String getDetalleTecnico() {
        return detalleTecnico;
    }
}
