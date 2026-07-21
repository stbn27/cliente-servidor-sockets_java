package common.exception;

public class ConflictoEdicionException extends Exception {

    private static final long serialVersionUID = 1L;

    private final int versionEsperada;
    private final int versionActual;

    public ConflictoEdicionException(String mensaje, int versionEsperada, int versionActual) {
        super(mensaje);
        this.versionEsperada = versionEsperada;
        this.versionActual = versionActual;
    }

    public int getVersionEsperada() {
        return versionEsperada;
    }

    public int getVersionActual() {
        return versionActual;
    }
}
