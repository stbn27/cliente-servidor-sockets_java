package servidor.replicacion;

import java.util.List;
import servidor.excepciones.ErrorAplicacion;
import servidor.modelos.OperacionPendienteInventario;
import servidor.repositorios.RepositorioOperacionesPendientes;
import servidor.util.ConsolaServidor;

/**
 * Intenta vaciar la cola local de operaciones pendientes hacia el principal.
 */
public final class SincronizadorInventarioReplica {

    private static final String MODULO = "SINCRONIZACION_REPLICA";

    private final ClientePrincipalXmlRpc clientePrincipal;
    private final RepositorioOperacionesPendientes repositorioPendientesHaciaPrincipal;

    /**
     * Crea el sincronizador.
     *
     * @param clientePrincipal cliente RPC hacia el principal.
     * @param repositorioPendientesHaciaPrincipal cola local de contingencia.
     */
    public SincronizadorInventarioReplica(ClientePrincipalXmlRpc clientePrincipal,
            RepositorioOperacionesPendientes repositorioPendientesHaciaPrincipal) {
        this.clientePrincipal = clientePrincipal;
        this.repositorioPendientesHaciaPrincipal = repositorioPendientesHaciaPrincipal;
    }

    /**
     * Intenta enviar al principal todas las operaciones pendientes.
     *
     * <p>Si el principal no esta disponible, la cola se conserva intacta.
     */
    public void sincronizarPendientes() {
        try {

            List<OperacionPendienteInventario> pendientes = repositorioPendientesHaciaPrincipal.listar();

            for (OperacionPendienteInventario operacion : pendientes) {
                clientePrincipal.enviarOperacionPendiente(operacion);
                repositorioPendientesHaciaPrincipal.eliminar(operacion.idOperacion());
                ConsolaServidor.info(MODULO,
                        "Pendiente entregado al principal: " + operacion.idOperacion()
                );
            }

        } catch (ErrorAplicacion excepcion) {
            ConsolaServidor.advertencia(MODULO,
                    "No fue posible vaciar la cola hacia el principal: " + excepcion.getMessage()
            );
        } catch (Exception excepcion) {
            ConsolaServidor.advertencia(MODULO,
                    "Se detecto un problema al sincronizar con el principal: " + excepcion.getMessage()
            );
        }
    }
}
