package cliente.config;

/**
 * Configuracion del cliente de consola.
 */
public final class ConfiguracionCliente {

    private static final String HOST_PRINCIPAL_POR_DEFECTO = "127.0.0.1";
    private static final int PUERTO_PRINCIPAL_POR_DEFECTO = 8080;
    private static final String HOST_REPLICA_POR_DEFECTO = "127.0.0.1";
    private static final int PUERTO_REPLICA_POR_DEFECTO = 8090;

    private final String hostPrincipal;
    private final int puertoPrincipal;
    private final String hostReplica;
    private final int puertoReplica;

    private ConfiguracionCliente(String hostPrincipal, int puertoPrincipal, String hostReplica, int puertoReplica) {
        this.hostPrincipal = hostPrincipal;
        this.puertoPrincipal = puertoPrincipal;
        this.hostReplica = hostReplica;
        this.puertoReplica = puertoReplica;
    }

    /**
     * Lee la configuracion desde propiedades del sistema.
     *
     * @return configuracion final del cliente.
     */
    public static ConfiguracionCliente desdeSistema() {
        String hostPrincipal = System.getProperty("host.principal", HOST_PRINCIPAL_POR_DEFECTO);
        int puertoPrincipal = Integer.parseInt(
                System.getProperty("puerto.principal", String.valueOf(PUERTO_PRINCIPAL_POR_DEFECTO))
        );
        String hostReplica = System.getProperty("host.replica", HOST_REPLICA_POR_DEFECTO);
        int puertoReplica = Integer.parseInt(
                System.getProperty("puerto.replica", String.valueOf(PUERTO_REPLICA_POR_DEFECTO))
        );
        return new ConfiguracionCliente(hostPrincipal, puertoPrincipal, hostReplica, puertoReplica);
    }

    public String getHostPrincipal() {
        return hostPrincipal;
    }

    public int getPuertoPrincipal() {
        return puertoPrincipal;
    }

    public String getHostReplica() {
        return hostReplica;
    }

    public int getPuertoReplica() {
        return puertoReplica;
    }
}
