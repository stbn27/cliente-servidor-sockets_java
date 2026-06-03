package cliente.util;

import java.util.List;

/**
 * Utilidad para construir tablas ASCII con anchos fijos.
 */
public final class TablaAscii {
    private TablaAscii() {
        // Utilidad estatica.
    }

    /**
     * Construye una tabla en formato ASCII.
     *
     * @param encabezados encabezados de columnas.
     * @param filas filas de datos.
     * @return tabla en texto con separadores.
     */
    public static String construirTabla(String[] encabezados, List<String[]> filas) {
        int[] anchos = calcularAnchuras(encabezados, filas);
        StringBuilder sb = new StringBuilder();
        sb.append(lineaSeparador(anchos)).append(System.lineSeparator());
        sb.append(filaConPadding(encabezados, anchos)).append(System.lineSeparator());
        sb.append(lineaSeparador(anchos)).append(System.lineSeparator());
        for (String[] fila : filas) {
            sb.append(filaConPadding(fila, anchos)).append(System.lineSeparator());
        }
        sb.append(lineaSeparador(anchos));
        return sb.toString();
    }

    /**
     * Calcula anchos maximos por columna.
     *
     * @param encabezados encabezados de columnas.
     * @param filas filas de datos.
     * @return arreglo de anchos.
     */
    private static int[] calcularAnchuras(String[] encabezados, List<String[]> filas) {
        int[] anchos = new int[encabezados.length];
        for (int i = 0; i < encabezados.length; i++) {
            anchos[i] = encabezados[i].length();
        }
        for (String[] fila : filas) {
            for (int i = 0; i < encabezados.length && i < fila.length; i++) {
                String valor = fila[i] == null ? "" : fila[i];
                if (valor.length() > anchos[i]) {
                    anchos[i] = valor.length();
                }
            }
        }
        return anchos;
    }

    /**
     * Construye una linea separadora con + y -.
     *
     * @param anchos anchos de columnas.
     * @return linea separadora.
     */
    private static String lineaSeparador(int[] anchos) {
        StringBuilder sb = new StringBuilder();
        sb.append("+");
        for (int ancho : anchos) {
            sb.append("-".repeat(ancho + 2)).append("+");
        }
        return sb.toString();
    }

    /**
     * Construye una fila con padding a la derecha.
     *
     * @param columnas columnas de la fila.
     * @param anchos anchos de columnas.
     * @return fila formateada.
     */
    private static String filaConPadding(String[] columnas, int[] anchos) {
        StringBuilder sb = new StringBuilder();
        sb.append("|");
        for (int i = 0; i < anchos.length; i++) {
            String valor = (i < columnas.length && columnas[i] != null) ? columnas[i] : "";
            sb.append(" ").append(padRight(valor, anchos[i])).append(" |");
        }
        return sb.toString();
    }

    /**
     * Rellena a la derecha con espacios.
     *
     * @param valor texto original.
     * @param ancho ancho deseado.
     * @return texto con espacios a la derecha.
     */
    private static String padRight(String valor, int ancho) {
        if (valor.length() >= ancho) {
            return valor;
        }
        return valor + " ".repeat(ancho - valor.length());
    }
}

