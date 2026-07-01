package servidor.rpc;

import java.net.InetAddress;
import org.apache.xmlrpc.WebServer;
import servidor.util.ConsolaServidor;

/**
 * Wrapper del WebServer XML-RPC de la replica.
 */
public final class ServidorXmlRpcReplica {

    private final String hostLocal;
    private final int puerto;
    private final ManejadorSistemaRpc manejadorSistema;
    private final ManejadorInventarioClinicoRpc manejadorInventario;
    private WebServer servidor;

    public ServidorXmlRpcReplica(String hostLocal, int puerto,
            ManejadorSistemaRpc manejadorSistema,
            ManejadorInventarioClinicoRpc manejadorInventario) {
        this.hostLocal = hostLocal;
        this.puerto = puerto;
        this.manejadorSistema = manejadorSistema;
        this.manejadorInventario = manejadorInventario;
    }

    public void iniciar() {
        try {
            servidor = new WebServer(puerto, InetAddress.getByName(hostLocal));
            servidor.addHandler("Sistema", manejadorSistema);
            servidor.addHandler("InventarioClinico", manejadorInventario);
            servidor.start();

        } catch (Exception excepcion) {
            ConsolaServidor.error("SERVIDOR_REPLICA",
                    "No fue posible iniciar la replica: " + excepcion.getMessage()
            );

            throw new IllegalStateException(excepcion);
        }
    }

    public void detener() {
        if (servidor != null) {
            servidor.shutdown();
        }
    }
}
