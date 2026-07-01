package servidor.servicios;

import java.util.List;
import servidor.excepciones.CodigosError;
import servidor.excepciones.ErrorAplicacion;
import servidor.modelos.MedicamentoInventario;
import servidor.modelos.OperacionPendienteInventario;
import servidor.modelos.ResultadoOperacionReplica;
import servidor.replicacion.SincronizadorInventarioReplica;
import servidor.repositorios.RepositorioInventarioClinico;
import servidor.repositorios.RepositorioOperacionesAplicadas;
import servidor.repositorios.RepositorioOperacionesPendientes;
import servidor.util.GeneradorIdOperacion;
import servidor.validacion.ValidadorEntrada;

/**
 * Servicio local del inventario replicado.
 */
public final class ServicioInventarioClinico {

    private static final String MODULO = "INVENTARIO_CLINICO_REPLICA";

    private final RepositorioInventarioClinico repositorio;
    private final RepositorioOperacionesPendientes repositorioPendientesHaciaPrincipal;
    private final RepositorioOperacionesAplicadas repositorioAplicadasDesdePrincipal;
    private final SincronizadorInventarioReplica sincronizador;

    public ServicioInventarioClinico(RepositorioInventarioClinico repositorio,
            RepositorioOperacionesPendientes repositorioPendientesHaciaPrincipal,
            RepositorioOperacionesAplicadas repositorioAplicadasDesdePrincipal,
            SincronizadorInventarioReplica sincronizador) {
        this.repositorio = repositorio;
        this.repositorioPendientesHaciaPrincipal = repositorioPendientesHaciaPrincipal;
        this.repositorioAplicadasDesdePrincipal = repositorioAplicadasDesdePrincipal;
        this.sincronizador = sincronizador;
    }

    public MedicamentoInventario consultarExistenciaMedicamento(String clave) throws ErrorAplicacion {
        sincronizador.sincronizarPendientes();
        ValidadorEntrada.validarTextoObligatorio(clave, "clave", MODULO);

        return repositorio.buscarPorClave(clave.trim());
    }

    public List<MedicamentoInventario> listarMedicamentosBajoMinimo() throws ErrorAplicacion {
        sincronizador.sincronizarPendientes();

        return repositorio.listarBajoMinimo();
    }

    /**
     * Aplica una entrada de inventario.
     *
     * <p>Se expone tanto para pruebas controladas como para la replicacion
     * originada por el servidor principal.
     *
     * @param clave clave del medicamento.
     * @param cantidad cantidad a sumar.
     * @param responsable actor que solicita la operacion.
     * @return medicamento actualizado en la replica.
     * @throws ErrorAplicacion si la operacion falla.
     */
    public ResultadoOperacionReplica registrarEntradaMedicamento(String clave, int cantidad, String responsable)
            throws ErrorAplicacion {
        sincronizador.sincronizarPendientes();
        ValidadorEntrada.validarTextoObligatorio(clave, "clave", MODULO);
        ValidadorEntrada.validarTextoObligatorio(responsable, "responsable", MODULO);
        ValidadorEntrada.validarEnteroPositivo(cantidad, "cantidad", MODULO);

        String idOperacion = GeneradorIdOperacion.generarId("REPLICA");
        MedicamentoInventario actualizado = repositorio.registrarEntrada(clave.trim(), cantidad);
        OperacionPendienteInventario pendiente = new OperacionPendienteInventario(
                idOperacion,
                clave.trim(),
                cantidad,
                responsable.trim(),
                GeneradorIdOperacion.generarMarcaTiempo(),
                "REPLICA"
        );
        repositorioPendientesHaciaPrincipal.guardar(pendiente);

        sincronizador.sincronizarPendientes();

        boolean siguePendiente = false;
        List<OperacionPendienteInventario> pendientes = repositorioPendientesHaciaPrincipal.listar();
        for (OperacionPendienteInventario operacion : pendientes) {
            if (operacion.idOperacion().equals(idOperacion)) {
                siguePendiente = true;
                break;
            }
        }

        if (siguePendiente) {
            return new ResultadoOperacionReplica(
                    CodigosError.SINCRONIZACION_PENDIENTE_CON_PRINCIPAL,
                    "La entrada se aplico en la replica y quedo pendiente de sincronizar con el principal.",
                    idOperacion,
                    actualizado,
                    false,
                    "La operacion permanecera en cola hasta que el principal vuelva a estar disponible."
            );
        }

        return new ResultadoOperacionReplica(
                CodigosError.OK,
                "La entrada se aplico en la replica y ya se sincronizo con el principal.",
                idOperacion,
                actualizado,
                true,
                "Sincronizacion inmediata completada."
        );
    }

    /**
     * Aplica una operacion enviada por el principal.
     *
     * <p>Usa el identificador remoto para evitar duplicar incrementos si la
     * misma operacion se reintenta por una desconexion intermedia.
     *
     * @param idOperacion identificador idempotente del principal.
     * @param clave clave del medicamento.
     * @param cantidad cantidad a sumar.
     * @param responsable actor que origino la operacion.
     * @return medicamento actualizado en la replica.
     * @throws ErrorAplicacion si falla la validacion o la persistencia.
     */
    public MedicamentoInventario registrarEntradaMedicamentoReplicada(String idOperacion, String clave,
            int cantidad, String responsable) throws ErrorAplicacion {
        ValidadorEntrada.validarTextoObligatorio(idOperacion, "idOperacion", MODULO);
        ValidadorEntrada.validarTextoObligatorio(clave, "clave", MODULO);
        ValidadorEntrada.validarTextoObligatorio(responsable, "responsable", MODULO);
        ValidadorEntrada.validarEnteroPositivo(cantidad, "cantidad", MODULO);

        if (repositorioAplicadasDesdePrincipal.yaAplicada(idOperacion)) {
            return repositorio.buscarPorClave(clave.trim());
        }

        MedicamentoInventario actualizado = repositorio.registrarEntrada(clave.trim(), cantidad);
        repositorioAplicadasDesdePrincipal.marcarAplicada(new OperacionPendienteInventario(
                idOperacion,
                clave.trim(),
                cantidad,
                responsable.trim(),
                GeneradorIdOperacion.generarMarcaTiempo(),
                "PRINCIPAL")
        );

        return actualizado;
    }

    /**
     * Devuelve la cola de operaciones que aun no ha podido recibir el
     * principal.
     *
     * @return lista local de pendientes hacia el principal.
     * @throws ErrorAplicacion si falla la lectura del archivo.
     */
    public List<OperacionPendienteInventario> obtenerOperacionesPendientesHaciaPrincipal() throws ErrorAplicacion {
        return repositorioPendientesHaciaPrincipal.listar();
    }

    /**
     * Elimina una operacion pendiente que el principal ya confirmo.
     *
     * @param idOperacion identificador confirmado por el principal.
     * @throws ErrorAplicacion si no puede actualizarse la cola.
     */
    public void confirmarOperacionPendienteHaciaPrincipal(String idOperacion) throws ErrorAplicacion {
        ValidadorEntrada.validarTextoObligatorio(idOperacion, "idOperacion", MODULO);
        repositorioPendientesHaciaPrincipal.eliminar(idOperacion);
    }
}
