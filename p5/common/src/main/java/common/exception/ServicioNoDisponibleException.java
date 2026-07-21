package common.exception;

public class ServicioNoDisponibleException extends Exception {

    private static final long serialVersionUID = 1L;

    public ServicioNoDisponibleException(String mensaje) {
        super(mensaje);
    }
}
