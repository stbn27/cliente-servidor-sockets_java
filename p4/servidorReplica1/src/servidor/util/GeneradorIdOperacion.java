package servidor.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * Genera identificadores para las operaciones distribuidas de la replica.
 */
public final class GeneradorIdOperacion {

    private static final DateTimeFormatter FORMATO = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private GeneradorIdOperacion() {
    }

    /**
     * Genera un identificador idempotente.
     *
     * @param prefijo nodo de origen.
     * @return identificador unico.
     */
    public static String generarId(String prefijo) {
        return prefijo + "-" + UUID.randomUUID().toString();
    }

    /**
     * Devuelve la marca de tiempo actual.
     *
     * @return fecha y hora formateadas.
     */
    public static String generarMarcaTiempo() {
        return LocalDateTime.now().format(FORMATO);
    }
}
