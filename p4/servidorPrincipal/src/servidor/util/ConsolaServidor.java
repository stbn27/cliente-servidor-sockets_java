package servidor.util;

/**
 * Utilidad simple para decorar mensajes del servidor en consola.
 */
public final class ConsolaServidor {

    private ConsolaServidor() {
    }

    public static void titulo(String texto) {
        System.out.println();
        System.out.println("/--------------------------------------------------------------\\");
        System.out.println("| " + ajustar(texto) + " |");
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

    private static String ajustar(String texto) {
        String valor = texto == null ? "" : texto;

        if (valor.length() >= 60) {
            return valor.substring(0, 60);
        }

        StringBuilder constructor = new StringBuilder(valor);

        while (constructor.length() < 60) {
            constructor.append(' ');
        }

        return constructor.toString();
    }
}
