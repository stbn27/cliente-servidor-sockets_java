package servidor.util;

/**
 * Utilidad para mostrar texto decorado en consola sin usar
 * separadores visuales simples como +, * o =.
 */
public final class DecoradorConsola {

    private static final int ANCHO = 70;
    private static final String LINEA = repetir("─", ANCHO);

    private DecoradorConsola() {
    }

    public static void titulo(String texto) {
        System.out.println();
        System.out.println("┌" + LINEA + "┐");
        System.out.println("│" + centrar(texto.toUpperCase(), ANCHO) + "│");
        System.out.println("└" + LINEA + "┘");
    }

    public static void seccion(String texto) {
        System.out.println();
        System.out.println("** " + texto);
        System.out.println("  " + LINEA);
    }

    public static void exito(String texto) {
        System.out.println("** " + texto);
    }

    public static void advertencia(String texto) {
        System.out.println("**  " + texto);
    }

    public static void error(String texto) {
        System.err.println("** " + texto);
    }

    public static void info(String texto) {
        System.out.println("**  " + texto);
    }

    private static String centrar(String texto, int ancho) {
        if (texto == null) {
            texto = "";
        }

        if (texto.length() >= ancho) {
            return texto.substring(0, ancho);
        }

        int espaciosTotales = ancho - texto.length();
        int izquierda = espaciosTotales / 2;
        int derecha = espaciosTotales - izquierda;

        return repetir(" ", izquierda) + texto + repetir(" ", derecha);
    }

    private static String repetir(String texto, int veces) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < veces; i++) {
            sb.append(texto);
        }
        return sb.toString();
    }
}