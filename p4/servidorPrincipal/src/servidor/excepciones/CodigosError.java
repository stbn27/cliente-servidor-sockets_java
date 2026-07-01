package servidor.excepciones;

/**
 * Centraliza los codigos de respuesta usados por el sistema distribuido.
 *
 * <p>Los codigos siguen una convencion legible para una practica escolar:
 * el prefijo numerico comunica el tipo de resultado y el texto explica
 * de forma breve la categoria del problema.
 */
public final class CodigosError {

    public static final String OK = "200_OK";
    public static final String REPLICA_PENDIENTE = "207_REPLICA_PENDIENTE";
    public static final String PARAMETRO_INVALIDO = "400_PARAMETRO_INVALIDO";
    public static final String OPERACION_INVALIDA = "400_OPERACION_INVALIDA";
    public static final String RECURSO_NO_ENCONTRADO = "404_RECURSO_NO_ENCONTRADO";
    public static final String METODO_NO_ENCONTRADO = "404_METODO_NO_ENCONTRADO";
    public static final String ERROR_EN_EL_SERVIDOR = "500_ERROR_EN_EL_SERVIDOR";
    public static final String ERROR_PERSISTENCIA = "500_ERROR_PERSISTENCIA";
    public static final String SERVIDOR_REPLICA_NO_DISPONIBLE = "503_SERVIDOR_REPLICA_NO_DISPONIBLE";

    private CodigosError() {
    }
}
