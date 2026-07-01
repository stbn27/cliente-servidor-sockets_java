package servidor.util;

/**
 * Decorador sencillo para mensajes de la replica.
 */
public final class ConsolaServidor {

    private ConsolaServidor() {
    }

    public static void titulo(String texto) {
        System.out.println();
        System.out.println("/--------------------------------------------------------------\\");
        System.out.println("| " + ajustar(texto, 60) + " |");
        System.out.println("\\--------------------------------------------------------------/");
    }

    public static void info(String modulo, String texto) {
        System.out.println("[INFO][" + modulo + "] " + texto);
    }

    public static void advertencia(String modulo, String texto) {
        System.out.println("[ADVERTENCIA][" + modulo + "] " + texto);
    }

    public static void error(String modulo, String texto) {
        System.out.println("[ERROR][" + modulo + "] " + texto);
    }

    private static String ajustar(String texto, int ancho) {
        String valor = texto == null ? "" : texto;
        if (valor.length() >= ancho) {
            return valor.substring(0, ancho);
        }
        StringBuilder constructor = new StringBuilder(valor);
        while (constructor.length() < ancho) {
            constructor.append(' ');
        }
        return constructor.toString();
    }
}
