package common.model;

public enum Categoria {
    TECNOLOGIA("Tecnología"),
    CIENCIA("Ciencia"),
    EDUCACION("Educación"),
    DEPORTES("Deportes"),
    CULTURA("Cultura"),
    ECONOMIA("Economía"),
    GENERAL("General");

    private final String nombreVisible;

    Categoria(String nombreVisible) {
        this.nombreVisible = nombreVisible;
    }

    public String getNombreVisible() {
        return nombreVisible;
    }

    @Override
    public String toString() {
        return nombreVisible;
    }
}
