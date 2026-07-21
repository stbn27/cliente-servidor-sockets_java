package common.dto;

import java.io.Serializable;

public final class ResultadoOperacion<T extends Serializable> implements Serializable {

    private static final long serialVersionUID = 1L;

    private final boolean exito;
    private final T dato;
    private final String mensaje;

    public static <T extends Serializable> ResultadoOperacion<T> exito(T dato) {
        return new ResultadoOperacion<>(true, dato, null);
    }

    public static <T extends Serializable> ResultadoOperacion<T> error(String mensaje) {
        return new ResultadoOperacion<>(false, null, mensaje);
    }

    private ResultadoOperacion(boolean exito, T dato, String mensaje) {
        this.exito = exito;
        this.dato = dato;
        this.mensaje = mensaje;
    }

    public boolean isExito() {
        return exito;
    }

    public T getDato() {
        return dato;
    }

    public String getMensaje() {
        return mensaje;
    }
}
