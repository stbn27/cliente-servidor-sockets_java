package servidor.config;

/**
 * Representa la configuracion operativa del servidor principal.
 *
 * <p>La configuracion se puede sobreescribir con propiedades del sistema
 * para facilitar pruebas en una sola maquina o en varias maquinas:
 * <pre>
 * ant run -Dhost.local=0.0.0.0 -Dpuerto.principal=8080 -Dhost.replica=127.0.0.1 -Dpuerto.replica=8090
 * </pre>
 */
public final class ConfiguracionServidor {

    private static final String HOST_LOCAL_POR_DEFECTO = "127.0.0.1";
    private static final int PUERTO_PRINCIPAL_POR_DEFECTO = 8080;
    private static final String HOST_REPLICA_POR_DEFECTO = "127.0.0.1";
    private static final int PUERTO_REPLICA_POR_DEFECTO = 8090;

    private final String hostLocal;
    private final int puertoPrincipal;
    private final String hostReplica;
    private final int puertoReplica;

    private ConfiguracionServidor(String hostLocal, int puertoPrincipal, String hostReplica, int puertoReplica) {
        this.hostLocal = hostLocal;
        this.puertoPrincipal = puertoPrincipal;
        this.hostReplica = hostReplica;
        this.puertoReplica = puertoReplica;
    }

    /**
     * Construye la configuracion usando propiedades del sistema.
     *
     * @return configuracion lista para ser usada por el servidor.
     */
    public static ConfiguracionServidor desdeSistema() {
        String hostLocal = System.getProperty("host.local", HOST_LOCAL_POR_DEFECTO);
        int puertoPrincipal = Integer.parseInt(
                System.getProperty("puerto.principal", String.valueOf(PUERTO_PRINCIPAL_POR_DEFECTO))
        );
        String hostReplica = System.getProperty("host.replica", HOST_REPLICA_POR_DEFECTO);
        int puertoReplica = Integer.parseInt(
                System.getProperty("puerto.replica", String.valueOf(PUERTO_REPLICA_POR_DEFECTO))
        );

        return new ConfiguracionServidor(hostLocal, puertoPrincipal, hostReplica, puertoReplica);
    }

    public String getHostLocal() {
        return hostLocal;
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
