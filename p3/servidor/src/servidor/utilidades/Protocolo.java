package servidor.utilidades;

import java.util.ArrayList;
import java.util.List;

/**
 * Utilidad para interpretar y construir mensajes del protocolo de texto.
 */
public final class Protocolo {
    /** Identificador del menu inicial. */
    public static final String MENU_INICIO = "MENU_INICIO";
    /** Identificador del menu posterior a seleccionar una funcion. */
    public static final String MENU_FUNCION = "MENU_FUNCION";
    /** Identificador del menu posterior a bloquear un asiento. */
    public static final String MENU_BLOQUEO = "MENU_BLOQUEO";

    /**
     * Constructor privado para evitar instanciacion.
     */
    private Protocolo() {
        // Clase de utilidad.
    }

    /**
     * Divide una linea del protocolo respetando caracteres escapados.
     * <br>
     * <b>Ejemplo: </b>
     * <pre>
     *     CAMBIAR_FECHA|2026-06-05 -> ["CAMBIAR_FECHA", "2026-06-05"]
     * </pre>
     *
     * <b>Nota: </b> Se espera que la linea divisora sea el caracter <b>|</b>
     *
     * @param linea linea recibida o enviada.
     * @return arreglo de partes interpretadas.
     */
    public static String[] dividirLinea(String linea) {
        if (linea == null) {
            return new String[0];
        }
        String contenido = linea.trim();
        if (contenido.isEmpty()) {
            return new String[0];
        }
        List<String> partes = new ArrayList<>();
        StringBuilder actual = new StringBuilder();
        boolean escapando = false;
        for (int indice = 0; indice < contenido.length(); indice++) {
            char caracter = contenido.charAt(indice);
            if (escapando) {
                actual.append(desescaparCaracter(caracter));
                escapando = false;
            } else if (caracter == '\\') {
                escapando = true;
            } else if (caracter == '|') {
                partes.add(actual.toString().trim());
                actual.setLength(0);
            } else {
                actual.append(caracter);
            }
        }
        if (escapando) {
            actual.append('\\');
        }
        partes.add(actual.toString().trim());
        return partes.toArray(new String[0]);
    }

    /**
     * Obtiene el comando principal en mayusculas.
     * <br>
     * <b>Ejemplo: </b>
     * <pre>
     *     ["CAMBIAR_FECHA", "2026-06-05"] -> CAMBIAR_FECHA
     * </pre>
     * @param partes partes separadas del mensaje.
     * @return comando en mayusculas o cadena vacia.
     */
    public static String comandoDe(String[] partes) {
        if (partes == null || partes.length == 0) {
            return "";
        }
        return partes[0].trim().toUpperCase();
    }

    /**
     * Valida la cantidad esperada de partes.
     *
     * @param partes partes del mensaje.
     * @param esperados cantidad exacta esperada.
     * @return {@code true} si la longitud coincide.
     */
    public static boolean validarLongitud(String[] partes, int esperados) {
        return partes != null && partes.length == esperados;
    }

    /**
     * Escapa un campo para poder enviarlo de forma segura dentro del protocolo.
     *
     * @param valor valor original.
     * @return valor escapado.
     */
    public static String escaparCampo(String valor) {
        if (valor == null) {
            return "";
        }
        return valor
                .replace("\\", "\\\\")
                .replace("|", "\\|")
                .replace("\r", "")
                .replace("\n", "\\n");
    }

    /**
     * Convierte un texto del protocolo a una representacion legible para consola.
     *
     * @param valor valor posiblemente escapado.
     * @return valor legible con saltos de linea reales.
     */
    public static String hacerLegible(String valor) {
        if (valor == null) {
            return "";
        }
        return valor.replace("\\n", System.lineSeparator()).replace("\\|", "|").replace("\\\\", "\\");
    }

    /**
     * Convierte una secuencia escapada a su caracter real.
     *
     * @param caracter caracter encontrado despues de una barra invertida.
     * @return caracter interpretado.
     */
    private static char desescaparCaracter(char caracter) {
        if (caracter == 'n') {
            return '\n';
        }
        return caracter;
    }
}
