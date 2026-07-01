package cliente.util;

/**
 * Decorador ASCII para menus y mensajes del cliente.
 */
public final class ConsolaDecoradora {

    private ConsolaDecoradora() {
    }

    public static void titulo(String texto) {
        System.out.println();
        System.out.println("/================================================================\\");
        System.out.println("| " + ajustar(texto) + " |");
        System.out.println("\\================================================================/");
    }

    public static void subtitulo(String texto) {
        System.out.println();
        System.out.println("/------------------------ " + texto + " ------------------------\\");
    }

    public static void info(String texto) {
        System.out.println("[INFO] " + texto);
    }

    public static void exito(String texto) {
        System.out.println("[OK] " + texto);
    }

    public static void advertencia(String texto) {
        System.out.println("[ADVERTENCIA] " + texto);
    }

    public static void error(String texto) {
        System.out.println("[ERROR] " + texto);
    }

    private static String ajustar(String texto) {
        String valor = texto == null ? "" : texto;
        if (valor.length() >= 62) {
            return valor.substring(0, 62);
        }
        StringBuilder constructor = new StringBuilder(valor);
        while (constructor.length() < 62) {
            constructor.append(' ');
        }
        return constructor.toString();
    }
}
