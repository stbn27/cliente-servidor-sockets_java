package servidor;

import servidor.config.ConfiguracionReplica;
import servidor.persistencia.ArchivoCsvUtil;
import servidor.replicacion.ClientePrincipalXmlRpc;
import servidor.replicacion.SincronizadorInventarioReplica;
import servidor.repositorios.RepositorioInventarioClinico;
import servidor.repositorios.RepositorioOperacionesAplicadas;
import servidor.repositorios.RepositorioOperacionesPendientes;
import servidor.rpc.ManejadorInventarioClinicoRpc;
import servidor.rpc.ManejadorSistemaRpc;
import servidor.rpc.ServidorXmlRpcReplica;
import servidor.servicios.ServicioInventarioClinico;
import servidor.util.ConsolaServidor;

/**
 * Punto de entrada del servidor replica.
 *
 * <p>La replica expone la categoria completa de inventario clinico para
 * demostrar replicacion parcial de procesos. El cliente final debe escribir
 * contra el principal; la replica mantiene la copia sincronizada mientras
 * este disponible.
 */
public final class Main {

    private Main() {
    }

    /**
     * Inicia el servidor replica y mantiene el proceso activo.
     *
     * @param argumentos argumentos opcionales de linea de comandos.
     */
    public static void main(String[] argumentos) {

        ConfiguracionReplica configuracion = ConfiguracionReplica.desdeSistema();

        RepositorioInventarioClinico repositorio = new RepositorioInventarioClinico(
                ArchivoCsvUtil.resolverRutaBase("datos/inventario_clinico")
        );

        RepositorioOperacionesPendientes repositorioPendientesHaciaPrincipal = new RepositorioOperacionesPendientes(
                ArchivoCsvUtil.resolverRutaBase("datos/inventario_clinico/operaciones_pendientes_hacia_principal.csv"),
                "INVENTARIO_CLINICO_REPLICA"
        );
        RepositorioOperacionesAplicadas repositorioAplicadasDesdePrincipal = new RepositorioOperacionesAplicadas(
                ArchivoCsvUtil.resolverRutaBase("datos/inventario_clinico/operaciones_aplicadas_desde_principal.csv"),
                "INVENTARIO_CLINICO_REPLICA"
        );

        ClientePrincipalXmlRpc clientePrincipal = new ClientePrincipalXmlRpc(
                configuracion.getHostPrincipal(),
                configuracion.getPuertoPrincipal()
        );

        SincronizadorInventarioReplica sincronizador = new SincronizadorInventarioReplica(
                clientePrincipal,
                repositorioPendientesHaciaPrincipal
        );

        ServicioInventarioClinico servicio = new ServicioInventarioClinico(
                repositorio,
                repositorioPendientesHaciaPrincipal,
                repositorioAplicadasDesdePrincipal,
                sincronizador
        );

        ServidorXmlRpcReplica servidor = new ServidorXmlRpcReplica(
                configuracion.getHostLocal(),
                configuracion.getPuertoReplica(),
                new ManejadorSistemaRpc(configuracion),
                new ManejadorInventarioClinicoRpc(servicio)
        );

        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                ConsolaServidor.info("SERVIDOR_REPLICA", "Apagando replica.");
                servidor.detener();
            }
        }));

        servidor.iniciar();
        ConsolaServidor.titulo("Servidor replica activo");
        ConsolaServidor.info("SERVIDOR_REPLICA",
                "Host de escucha: " + configuracion.getHostLocal()
                + " | Puerto: " + configuracion.getPuertoReplica()
        );
        ConsolaServidor.info("SERVIDOR_REPLICA",
                "Principal configurado en " + configuracion.getHostPrincipal()
                + ":" + configuracion.getPuertoPrincipal()
        );
        ConsolaServidor.info("SERVIDOR_REPLICA",
                "Categoria replicada: inventario clinico."
        );

        while (true) {
            try {
                Thread.sleep(60000L);
            } catch (InterruptedException excepcion) {
                Thread.currentThread().interrupt();
                ConsolaServidor.advertencia("SERVIDOR_REPLICA",
                        "Proceso interrumpido. Se iniciara el apagado.");
                break;
            }
        }

        servidor.detener();
    }
}
