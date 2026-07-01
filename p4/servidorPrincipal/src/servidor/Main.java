package servidor;

import servidor.config.ConfiguracionServidor;
import servidor.persistencia.ArchivoCsvUtil;
import servidor.replicacion.ClienteReplicaXmlRpc;
import servidor.repositorios.RepositorioInventarioClinico;
import servidor.repositorios.RepositorioMovilidadUrbana;
import servidor.repositorios.RepositorioOperacionesAplicadas;
import servidor.repositorios.RepositorioOperacionesPendientes;
import servidor.repositorios.RepositorioRedesSociales;
import servidor.rpc.ManejadorInventarioClinicoRpc;
import servidor.rpc.ManejadorMovilidadUrbanaRpc;
import servidor.rpc.ManejadorRedesSocialesRpc;
import servidor.rpc.ManejadorSistemaRpc;
import servidor.rpc.ServidorXmlRpc;
import servidor.servicios.ServicioInventarioClinico;
import servidor.servicios.ServicioMovilidadUrbana;
import servidor.servicios.ServicioRedesSociales;
import servidor.util.ConsolaServidor;

/**
 * Punto de entrada del servidor principal.
 *
 * <p>Este proceso expone tres categorias de servicios por XML-RPC:
 * redes sociales, movilidad urbana e inventario clinico. La categoria
 * de inventario clinico se replica parcialmente hacia el servidor replica
 * para demostrar tolerancia a fallas con una politica simple y explicable.
 *
 */
public final class Main {

    private Main() {
    }

    /**
     * Inicia el servidor principal y mantiene vivo el proceso.
     *
     * @param argumentos argumentos de linea de comandos. No se requieren
     * para la ejecucion normal.
     */
    public static void main(String[] argumentos) {

        // Inicializar el parámetros para el servidor ------------------------------------------------------
        ConfiguracionServidor configuracion = ConfiguracionServidor.desdeSistema();

        // Repositorios de los servicios -------------------------------------------------------------------
        RepositorioRedesSociales repositorioRedes = new RepositorioRedesSociales(
                ArchivoCsvUtil.resolverRutaBase("datos/redes_sociales")
        );
        RepositorioMovilidadUrbana repositorioMovilidad = new RepositorioMovilidadUrbana(
                ArchivoCsvUtil.resolverRutaBase("datos/movilidad_urbana")
        );
        RepositorioInventarioClinico repositorioInventario = new RepositorioInventarioClinico(
                ArchivoCsvUtil.resolverRutaBase("datos/inventario_clinico")
        );

        // Repositorios para las replicas pendientes ------------------------------------------------------
        RepositorioOperacionesPendientes repositorioPendientesHaciaReplica = new RepositorioOperacionesPendientes(
                ArchivoCsvUtil.resolverRutaBase("datos/inventario_clinico/operaciones_pendientes_hacia_replica.csv"),
                "INVENTARIO_CLINICO"
        );
        // Operaciones ya aplicadas remotamente a ambos servidores ----------------------------------------
        RepositorioOperacionesAplicadas repositorioAplicadasDesdeReplica = new RepositorioOperacionesAplicadas(
                ArchivoCsvUtil.resolverRutaBase("datos/inventario_clinico/operaciones_aplicadas_desde_replica.csv"),
                "INVENTARIO_CLINICO"
        );

        // Servidor de replica ----------------------------------------------------------------------------
        ClienteReplicaXmlRpc clienteReplica = new ClienteReplicaXmlRpc(
                configuracion.getHostReplica(),
                configuracion.getPuertoReplica()
        );

        // Servicios --------------------------------------------------------------------------------------
        ServicioRedesSociales servicioRedes = new ServicioRedesSociales(repositorioRedes);
        ServicioMovilidadUrbana servicioMovilidad = new ServicioMovilidadUrbana(repositorioMovilidad);
        ServicioInventarioClinico servicioInventario = new ServicioInventarioClinico(
                repositorioInventario,
                clienteReplica,
                repositorioPendientesHaciaReplica,
                repositorioAplicadasDesdeReplica
        );

        // Inicialización del Servidor RPC ----------------------------------------------------------------
        ServidorXmlRpc servidor = new ServidorXmlRpc(
                configuracion.getHostLocal(),
                configuracion.getPuertoPrincipal(),
                new ManejadorSistemaRpc(configuracion),
                new ManejadorRedesSocialesRpc(servicioRedes),
                new ManejadorMovilidadUrbanaRpc(servicioMovilidad),
                new ManejadorInventarioClinicoRpc(servicioInventario)
        );

        // Cierra el servidor principal, se hace limpieza final y se detiene el servidor de red de manera controlada.
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                ConsolaServidor.info("SERVIDOR_PRINCIPAL", "Apagando servidor principal.");
                servidor.detener();
            }
        }));

        // Inicialización final -----------------------------------------------------------------------------
        servidor.iniciar();

        // Logs de inicialización del servidor --------------------------------------------------------------
        ConsolaServidor.titulo("Servidor principal activo");
        ConsolaServidor.info("SERVIDOR_PRINCIPAL",
                "Host de escucha: " + configuracion.getHostLocal()
                + " | Puerto: " + configuracion.getPuertoPrincipal()
        );
        ConsolaServidor.info("SERVIDOR_PRINCIPAL",
                "Replica configurada en " + configuracion.getHostReplica()
                + ":" + configuracion.getPuertoReplica()
        );
        ConsolaServidor.info("SERVIDOR_PRINCIPAL",
                "Categoria replicada: inventario clinico."
        );

        while (true) {
            try {
                Thread.sleep(60000L);
            } catch (InterruptedException excepcion) {
                Thread.currentThread().interrupt();

                ConsolaServidor.advertencia(
                        "SERVIDOR_PRINCIPAL",
                        "Proceso interrumpido. Se iniciara el apagado."
                );
                break;
            }
        }

        servidor.detener();
    }
}
