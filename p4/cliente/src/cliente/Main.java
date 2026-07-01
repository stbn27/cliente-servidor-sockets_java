package cliente;

import cliente.config.ConfiguracionCliente;
import cliente.menu.AplicacionClienteConsola;
import cliente.rpc.ClienteRpc;
import cliente.util.ConsolaDecoradora;

/**
 * <b>Punto de entrada del cliente por consola.</b>
 *
 * <p>El cliente usa XML-RPC clasico con {@code XmlRpcClient}, envia
 * parametros en {@code Vector} y mantiene un menu persistente hasta que el
 * usuario elige salir.
 *
 * @author Esteban Nuñez José Julian
 * @version 1.0.2
 * @since 1.0.0
 */
public final class Main {

    private Main() {
    }

    /**
     * Inicia el menu interactivo del cliente.
     *
     * @param argumentos argumentos opcionales de linea de comandos.
     */
    public static void main(String[] argumentos) {

        ConfiguracionCliente configuracion = ConfiguracionCliente.desdeSistema();

        //Información de los servidores
        ConsolaDecoradora.titulo("Cliente distribuido por consola");
        ConsolaDecoradora.info("Servidor principal configurado en " + configuracion.getHostPrincipal() + ":" + configuracion.getPuertoPrincipal());
        ConsolaDecoradora.info("Servidor replica configurado en " + configuracion.getHostReplica() + ":" + configuracion.getPuertoReplica());

        // Inicialización del cliente RPC - Host y Puertos
        ClienteRpc clienteRpc = new ClienteRpc(
                configuracion.getHostPrincipal(),
                configuracion.getPuertoPrincipal(),
                configuracion.getHostReplica(),
                configuracion.getPuertoReplica()
        );

        // Inicialización de la IU(consola)
        AplicacionClienteConsola aplicacion = new AplicacionClienteConsola(clienteRpc);
        aplicacion.iniciar();
    }
}
