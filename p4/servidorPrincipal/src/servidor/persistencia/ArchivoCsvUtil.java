package servidor.persistencia;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import servidor.excepciones.ErrorPersistencia;

/**
 * Utilidad comun para leer y escribir archivos CSV simples con separador
 * vertical.
 *
 * <p>La practica evita dependencias extra. Por eso se usa un formato
 * deliberadamente sencillo y facil de inspeccionar desde un editor de texto.
 */
public final class ArchivoCsvUtil {

    private ArchivoCsvUtil() {
    }

    /**
     * Resuelve una ruta relativa al directorio del proyecto.
     *
     * @param rutaRelativa ruta desde la raiz del subproyecto.
     * @return ruta absoluta normalizada.
     */
    public static Path resolverRutaBase(String rutaRelativa) {
        return Paths.get(rutaRelativa).toAbsolutePath().normalize();
    }

    /**
     * Lee todas las filas de datos de un archivo CSV, excluyendo encabezados
     * y lineas vacias.
     *
     * @param ruta archivo a leer.
     * @param modulo modulo que realiza la lectura.
     * @return lista de columnas por fila.
     * @throws ErrorPersistencia si el archivo no se puede leer.
     */
    public static List<String[]> leerRegistros(Path ruta, String modulo) throws ErrorPersistencia {
        try {
            if (!Files.exists(ruta)) {
                throw new ErrorPersistencia(
                        "No fue posible localizar el dataset requerido.",
                        "Archivo inexistente: " + ruta,
                        modulo
                );
            }
            List<String> lineas = Files.readAllLines(ruta, StandardCharsets.UTF_8);
            List<String[]> registros = new ArrayList<>();
            for (int indice = 0; indice < lineas.size(); indice++) {
                String linea = lineas.get(indice).trim();
                if (linea.isEmpty() || linea.startsWith("#")) {
                    continue;
                }
                if (indice == 0 && linea.toLowerCase().contains("|")) {
                    continue;
                }
                registros.add(linea.split("\\|", -1));
            }
            return registros;
        } catch (IOException excepcion) {
            throw new ErrorPersistencia(
                    "Ocurrio un problema al leer la informacion del servidor.",
                    excepcion.getMessage(),
                    modulo);
        }
    }

    /**
     * Escribe un archivo CSV completo con encabezado y filas.
     *
     * @param ruta ruta a escribir.
     * @param lineas lineas completas del archivo.
     * @param modulo modulo responsable.
     * @throws ErrorPersistencia si no se puede guardar el archivo.
     */
    public static synchronized void escribirArchivo(Path ruta, List<String> lineas, String modulo)
            throws ErrorPersistencia {
        try {
            Path directorio = ruta.getParent();
            if (directorio != null) {
                Files.createDirectories(directorio);
            }
            Files.write(ruta, lineas, StandardCharsets.UTF_8);
        } catch (IOException excepcion) {
            throw new ErrorPersistencia(
                    "No fue posible guardar los cambios del servidor.",
                    excepcion.getMessage(),
                    modulo);
        }
    }
}
