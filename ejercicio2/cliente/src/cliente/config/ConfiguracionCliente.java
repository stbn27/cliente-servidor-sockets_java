package cliente.config;

/**
 * Configuración general del cliente.
 */
public final class ConfiguracionCliente {

    public static final String HOST_PREDETERMINADO = "127.0.0.1";
    public static final int PUERTO = 8080;
    public static final String RUTA_RPC = "/RPC2";

    private ConfiguracionCliente() {
    }

    public static String construirUrl(String host) {
        return "http://" + host + ":" + PUERTO;
    }
}