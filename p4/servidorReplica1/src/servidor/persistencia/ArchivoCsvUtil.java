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
 * Utilidad de acceso a archivos CSV de la replica.
 */
public final class ArchivoCsvUtil {

    private ArchivoCsvUtil() {
    }

    public static Path resolverRutaBase(String rutaRelativa) {
        return Paths.get(rutaRelativa).toAbsolutePath().normalize();
    }

    public static List<String[]> leerRegistros(Path ruta, String modulo) throws ErrorPersistencia {
        try {
            if (!Files.exists(ruta)) {
                throw new ErrorPersistencia(
                        "No se encontro el dataset requerido por la replica.",
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
                    "No fue posible leer la informacion local de la replica.",
                    excepcion.getMessage(),
                    modulo
            );
        }
    }

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
                    "No fue posible guardar los cambios en la replica.",
                    excepcion.getMessage(),
                    modulo
            );
        }
    }
}
