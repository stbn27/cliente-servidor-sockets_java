package cliente.util;

import java.util.ArrayList;
import java.util.List;

/**
 * Ofrece utilidades para interpretar respuestas del protocolo textual del servidor.
 */
public final class ProtocoloCliente {

    /**
     * Evita la instanciacion de la clase utilitaria.
     */
    private ProtocoloCliente() {
        // Clase utilitaria.
    }

    /**
     * Divide una linea del protocolo respetando caracteres escapados.
     *
     * @param linea linea recibida del servidor.
     * @return arreglo con las partes separadas por el delimitador real.
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
     * Convierte secuencias escapadas del protocolo a una forma legible en consola.
     *
     * @param valor valor textual del protocolo.
     * @return valor con saltos de linea y separadores reales.
     */
    public static String hacerLegible(String valor) {
        if (valor == null) {
            return "";
        }
        return valor.replace("\\n", System.lineSeparator())
                .replace("\\|", "|")
                .replace("\\\\", "\\");
    }

    /**
     * Interpreta un caracter escapado dentro de la linea del protocolo.
     *
     * @param caracter caracter encontrado despues de una barra invertida.
     * @return caracter ya desescapado.
     */
    private static char desescaparCaracter(char caracter) {
        if (caracter == 'n') {
            return '\n';
        }
        return caracter;
    }
}
