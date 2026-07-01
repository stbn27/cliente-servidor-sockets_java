package servidor.config;

/**
 * Configuracion operativa del servidor replica.
 */
public final class ConfiguracionReplica {

    private static final String HOST_LOCAL_POR_DEFECTO = "127.0.0.1";
    private static final int PUERTO_REPLICA_POR_DEFECTO = 8090;
    private static final String HOST_PRINCIPAL_POR_DEFECTO = "127.0.0.1";
    private static final int PUERTO_PRINCIPAL_POR_DEFECTO = 8080;

    private final String hostLocal;
    private final int puertoReplica;
    private final String hostPrincipal;
    private final int puertoPrincipal;

    private ConfiguracionReplica(String hostLocal, int puertoReplica, String hostPrincipal, int puertoPrincipal) {
        this.hostLocal = hostLocal;
        this.puertoReplica = puertoReplica;
        this.hostPrincipal = hostPrincipal;
        this.puertoPrincipal = puertoPrincipal;
    }

    /**
     * Construye la configuracion desde propiedades del sistema.
     *
     * @return configuracion final.
     */
    public static ConfiguracionReplica desdeSistema() {

        String hostLocal = System.getProperty("host.local", HOST_LOCAL_POR_DEFECTO);
        int puertoReplica = Integer.parseInt(
                System.getProperty("puerto.replica", String.valueOf(PUERTO_REPLICA_POR_DEFECTO))
        );
        String hostPrincipal = System.getProperty("host.principal", HOST_PRINCIPAL_POR_DEFECTO);
        int puertoPrincipal = Integer.parseInt(
                System.getProperty("puerto.principal", String.valueOf(PUERTO_PRINCIPAL_POR_DEFECTO))
        );

        return new ConfiguracionReplica(hostLocal, puertoReplica, hostPrincipal, puertoPrincipal);
    }

    public String getHostLocal() {
        return hostLocal;
    }

    public int getPuertoReplica() {
        return puertoReplica;
    }

    public String getHostPrincipal() {
        return hostPrincipal;
    }

    public int getPuertoPrincipal() {
        return puertoPrincipal;
    }
}
