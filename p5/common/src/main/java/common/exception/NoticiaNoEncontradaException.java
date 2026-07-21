package common.exception;

public class NoticiaNoEncontradaException extends Exception {

    private static final long serialVersionUID = 1L;

    public NoticiaNoEncontradaException(String mensaje) {
        super(mensaje);
    }
}
