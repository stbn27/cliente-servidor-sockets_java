package servidor.excepciones;

/**
 * Excepcion base de negocio para el servidor principal.
 *
 * <p>Todo error controlado del sistema se transforma en esta jerarquia
 * antes de convertirse a una respuesta XML-RPC entendible por el cliente.
 */
public class ErrorAplicacion extends Exception {

    private final String codigo;
    private final String modulo;
    private final String detalleTecnico;

    /**
     * Crea un error controlado con contexto.
     *
     * @param codigo codigo legible del error.
     * @param mensaje mensaje amigable para el usuario final.
     * @param detalleTecnico detalle controlado para diagnostico.
     * @param modulo nombre del modulo que fallo.
     */
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
