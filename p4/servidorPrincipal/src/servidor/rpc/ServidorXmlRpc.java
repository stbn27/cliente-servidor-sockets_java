package servidor.rpc;

import java.net.InetAddress;
import org.apache.xmlrpc.WebServer;
import servidor.util.ConsolaServidor;

/**
 * Envuelve la configuracion del servidor XML-RPC clasico basado en WebServer.
 */
public final class ServidorXmlRpc {

    private final String hostLocal;
    private final int puerto;
    private final ManejadorSistemaRpc manejadorSistema;
    private final ManejadorRedesSocialesRpc manejadorRedes;
    private final ManejadorMovilidadUrbanaRpc manejadorMovilidad;
    private final ManejadorInventarioClinicoRpc manejadorInventario;
    private WebServer servidor;

    public ServidorXmlRpc(String hostLocal, int puerto,
            ManejadorSistemaRpc manejadorSistema,
            ManejadorRedesSocialesRpc manejadorRedes,
            ManejadorMovilidadUrbanaRpc manejadorMovilidad,
            ManejadorInventarioClinicoRpc manejadorInventario)
    {
        this.hostLocal = hostLocal;
        this.puerto = puerto;
        this.manejadorSistema = manejadorSistema;
        this.manejadorRedes = manejadorRedes;
        this.manejadorMovilidad = manejadorMovilidad;
        this.manejadorInventario = manejadorInventario;
    }

    /**
     * Registra handlers y arranca el servidor.
     */
    public void iniciar() {
        try {
            servidor = new WebServer(puerto, InetAddress.getByName(hostLocal));

            servidor.addHandler("Sistema", manejadorSistema);
            servidor.addHandler("RedesSociales", manejadorRedes);
            servidor.addHandler("MovilidadUrbana", manejadorMovilidad);
            servidor.addHandler("InventarioClinico", manejadorInventario);

            servidor.start();
        } catch (Exception excepcion) {
            ConsolaServidor.error(
                    "SERVIDOR_PRINCIPAL",
                    "No fue posible iniciar el servidor XML-RPC: " + excepcion.getMessage()
            );

            throw new IllegalStateException(excepcion);
        }
    }

    /**
     * Detiene el listener XML-RPC si esta activo.
     */
    public void detener() {
        if (servidor != null) {
            servidor.shutdown();
        }
    }
}
