package servidor.replicacion;

import java.util.List;
import servidor.excepciones.ErrorPersistencia;
import servidor.modelos.OperacionPendienteInventario;
import servidor.repositorios.RepositorioInventarioClinico;
import servidor.repositorios.RepositorioOperacionesAplicadas;
import servidor.repositorios.RepositorioOperacionesPendientes;
import servidor.util.ConsolaServidor;

/**
 * Reconciliador bidireccional del inventario entre principal y replica.
 */
public final class SincronizadorInventarioPrincipal {

    private static final String MODULO = "SINCRONIZACION_PRINCIPAL";

    private final ClienteReplicaXmlRpc clienteReplica;
    private final RepositorioInventarioClinico repositorioInventario;
    private final RepositorioOperacionesPendientes repositorioPendientesHaciaReplica;
    private final RepositorioOperacionesAplicadas repositorioAplicadasDesdeReplica;

    /**
     * Crea el sincronizador.
     *
     * @param clienteReplica cliente RPC hacia la replica.
     * @param repositorioInventario repositorio local del inventario.
     * @param repositorioPendientesHaciaReplica cola local de operaciones no confirmadas por la replica.
     * @param repositorioAplicadasDesdeReplica registro local de operaciones ya importadas desde la replica.
     */
    public SincronizadorInventarioPrincipal(ClienteReplicaXmlRpc clienteReplica,
            RepositorioInventarioClinico repositorioInventario,
            RepositorioOperacionesPendientes repositorioPendientesHaciaReplica,
            RepositorioOperacionesAplicadas repositorioAplicadasDesdeReplica) {
        this.clienteReplica = clienteReplica;
        this.repositorioInventario = repositorioInventario;
        this.repositorioPendientesHaciaReplica = repositorioPendientesHaciaReplica;
        this.repositorioAplicadasDesdeReplica = repositorioAplicadasDesdeReplica;
    }

    /**
     * Intenta reconciliar ambas colas antes de una operacion de inventario.
     *
     * <p>El proceso hace dos pasos:
     * <ol>
     * <li>envia los pendientes del principal hacia la replica;</li>
     * <li>importa los pendientes que la replica conserva hacia el principal.</li>
     * </ol>
     *
     * <p>Si algun paso falla, el sistema registra el problema en consola y la
     * operacion del cliente continua con el mejor estado local disponible.
     */
    public void sincronizarPendientes() {
        enviarPendientesHaciaReplica();
        importarPendientesDesdeReplica();
    }

    private void enviarPendientesHaciaReplica() {
        try {
            List<OperacionPendienteInventario> pendientes = repositorioPendientesHaciaReplica.listar();
            for (OperacionPendienteInventario operacion : pendientes) {
                clienteReplica.replicarEntradaMedicamento(
                        operacion.idOperacion(),
                        operacion.clave(),
                        operacion.cantidad(),
                        operacion.responsable()
                );
                repositorioPendientesHaciaReplica.eliminar(operacion.idOperacion());
                ConsolaServidor.info(MODULO,
                        "Pendiente enviado a replica: " + operacion.idOperacion()
                );
            }
        } catch (Exception excepcion) {
            ConsolaServidor.advertencia(MODULO,
                    "No fue posible vaciar la cola hacia la replica: " + excepcion.getMessage());
        }
    }

    private void importarPendientesDesdeReplica() {
        try {
            List<OperacionPendienteInventario> pendientesReplica = clienteReplica.obtenerOperacionesPendientesHaciaPrincipal();
            for (OperacionPendienteInventario operacion : pendientesReplica) {
                if (!repositorioAplicadasDesdeReplica.yaAplicada(operacion.idOperacion())) {
                    repositorioInventario.registrarEntrada(operacion.clave(), operacion.cantidad());
                    repositorioAplicadasDesdeReplica.marcarAplicada(operacion);
                    ConsolaServidor.info(MODULO,
                            "Operacion importada desde replica: " + operacion.idOperacion()
                    );
                }
                clienteReplica.confirmarOperacionPendienteHaciaPrincipal(operacion.idOperacion());
            }
        } catch (ErrorPersistencia excepcion) {
            ConsolaServidor.advertencia(MODULO,
                    "No fue posible registrar una operacion importada desde replica: " + excepcion.getMessage()
            );
        } catch (Exception excepcion) {
            ConsolaServidor.advertencia(MODULO,
                    "No fue posible reconciliar pendientes provenientes de replica: " + excepcion.getMessage()
            );
        }
    }
}
