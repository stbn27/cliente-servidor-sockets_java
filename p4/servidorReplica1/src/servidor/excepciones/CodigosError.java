package servidor.excepciones;

/**
 * Codigos estandarizados de la replica.
 */
public final class CodigosError {

    public static final String OK = "200_OK";
    public static final String SINCRONIZACION_PENDIENTE_CON_PRINCIPAL = "207_SINCRONIZACION_PENDIENTE_CON_PRINCIPAL";
    public static final String PARAMETRO_INVALIDO = "400_PARAMETRO_INVALIDO";
    public static final String RECURSO_NO_ENCONTRADO = "404_RECURSO_NO_ENCONTRADO";
    public static final String ERROR_EN_EL_SERVIDOR = "500_ERROR_EN_EL_SERVIDOR";
    public static final String ERROR_PERSISTENCIA = "500_ERROR_PERSISTENCIA";

    private CodigosError() {
    }
}
