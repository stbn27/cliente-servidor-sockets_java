package servidor.utilidades;

import servidor.modelo.Asiento;
import servidor.modelo.FuncionCine;
import servidor.modelo.Pelicula;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * Genera tablas ASCII para mostrar funciones y asientos por consola.
 */
public final class TablaAsciiUtil {

    /**
     * Constructor privado para evitar instanciacion.
     */
    private TablaAsciiUtil() {
        // Clase de utilidad.
    }

    /**
     * Genera la tabla ASCII de funciones.
     *
     * @param funciones lista de funciones a imprimir.
     * @param peliculas mapa de peliculas disponibles.
     * @return tabla ASCII completa.
     */
    public static String generarTablaFunciones(List<FuncionCine> funciones, Map<String, Pelicula> peliculas) {
        List<String[]> filas = new ArrayList<>();
        List<FuncionCine> ordenadas = new ArrayList<>(funciones);
        ordenadas.sort(Comparator.comparing(FuncionCine::getFechaComoLocalDate)
                .thenComparing(FuncionCine::getHoraComoLocalTime)
                .thenComparing(FuncionCine::getIdFuncion));
        for (FuncionCine funcion : ordenadas) {
            Pelicula pelicula = peliculas.get(funcion.getIdPelicula());
            filas.add(new String[]{
                    funcion.getIdFuncion(),
                    pelicula != null ? pelicula.getTitulo() : "Desconocida",
                    funcion.getSala(),
                    funcion.getFecha(),
                    funcion.getHora()
            });
        }
        return generarTabla(new String[]{"ID", "Pelicula", "Sala", "Fecha", "Hora"}, filas);
    }

    /**
     * Genera la tabla ASCII de asientos.
     *
     * @param asientos coleccion de asientos.
     * @return tabla ASCII completa.
     */
    public static String generarTablaAsientos(Collection<Asiento> asientos) {
        List<String[]> filas = new ArrayList<>();
        List<Asiento> ordenados = new ArrayList<>(asientos);
        ordenados.sort(Comparator.comparing(Asiento::getNumero));
        for (Asiento asiento : ordenados) {
            filas.add(new String[]{asiento.getNumero(), asiento.getEstado().name()});
        }
        return generarTabla(new String[]{"Asiento", "Estado"}, filas);
    }

    /**
     * Construye una tabla ASCII generica.
     *
     * @param encabezados encabezados de columnas.
     * @param filas filas de contenido.
     * @return tabla ASCII.
     */
    private static String generarTabla(String[] encabezados, List<String[]> filas) {
        int[] anchos = calcularAnchos(encabezados, filas);
        String separador = construirSeparador(anchos);
        StringBuilder tabla = new StringBuilder();
        tabla.append(separador).append('\n');
        tabla.append(construirFila(encabezados, anchos)).append('\n');
        tabla.append(separador);
        for (String[] fila : filas) {
            tabla.append('\n').append(construirFila(fila, anchos));
        }
        tabla.append('\n').append(separador);
        return tabla.toString();
    }

    /**
     * Calcula el ancho de cada columna.
     *
     * @param encabezados encabezados de columnas.
     * @param filas filas de contenido.
     * @return anchos por columna.
     */
    private static int[] calcularAnchos(String[] encabezados, List<String[]> filas) {
        int[] anchos = new int[encabezados.length];
        for (int indice = 0; indice < encabezados.length; indice++) {
            anchos[indice] = encabezados[indice].length();
        }
        for (String[] fila : filas) {
            for (int indice = 0; indice < encabezados.length && indice < fila.length; indice++) {
                anchos[indice] = Math.max(anchos[indice], fila[indice] != null ? fila[indice].length() : 0);
            }
        }
        return anchos;
    }

    /**
     * Construye la linea separadora de una tabla.
     *
     * @param anchos anchos por columna.
     * @return linea separadora.
     */
    private static String construirSeparador(int[] anchos) {
        StringBuilder separador = new StringBuilder("+");
        for (int ancho : anchos) {
            separador.append("-".repeat(ancho + 2)).append('+');
        }
        return separador.toString();
    }

    /**
     * Construye una fila con padding a la derecha.
     *
     * @param columnas columnas de la fila.
     * @param anchos anchos por columna.
     * @return fila renderizada.
     */
    private static String construirFila(String[] columnas, int[] anchos) {
        StringBuilder fila = new StringBuilder("|");
        for (int indice = 0; indice < anchos.length; indice++) {
            String valor = indice < columnas.length && columnas[indice] != null ? columnas[indice] : "";
            fila.append(' ').append(ajustarDerecha(valor, anchos[indice])).append(" |");
        }
        return fila.toString();
    }

    /**
     * Ajusta un texto al ancho indicado usando espacios a la derecha.
     *
     * @param valor texto original.
     * @param ancho ancho final requerido.
     * @return texto ajustado.
     */
    private static String ajustarDerecha(String valor, int ancho) {
        if (valor.length() >= ancho) {
            return valor;
        }
        return valor + " ".repeat(ancho - valor.length());
    }
}
