package servidor.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * Genera identificadores unicos para operaciones distribuidas de inventario.
 */
public final class GeneradorIdOperacion {

    private static final DateTimeFormatter FORMATO = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private GeneradorIdOperacion() {
    }

    /**
     * Genera un identificador idempotente.
     *
     * @param prefijo nodo que origino el cambio.
     * @return identificador unico legible.
     */
    public static String generarId(String prefijo) {
        return prefijo + "-" + UUID.randomUUID().toString();
    }

    /**
     * Devuelve la marca de tiempo estandar usada en los archivos de
     * sincronizacion.
     *
     * @return fecha y hora actual formateada.
     */
    public static String generarMarcaTiempo() {
        return LocalDateTime.now().format(FORMATO);
    }
}
