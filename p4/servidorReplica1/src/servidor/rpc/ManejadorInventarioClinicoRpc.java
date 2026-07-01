package servidor.rpc;

import java.util.Hashtable;
import java.util.List;
import java.util.Vector;
import servidor.excepciones.ErrorAplicacion;
import servidor.modelos.MedicamentoInventario;
import servidor.modelos.OperacionPendienteInventario;
import servidor.modelos.ResultadoOperacionReplica;
import servidor.servicios.ServicioInventarioClinico;
import servidor.util.RespuestaRpcUtil;

/**
 * Handler XML-RPC de la replica para inventario clinico.
 */
public final class ManejadorInventarioClinicoRpc {

    private static final String MODULO = "INVENTARIO_CLINICO_REPLICA";

    private final ServicioInventarioClinico servicio;

    public ManejadorInventarioClinicoRpc(ServicioInventarioClinico servicio) {
        this.servicio = servicio;
    }

    public Hashtable consultarExistenciaMedicamento(String clave) {
        try {
            return RespuestaRpcUtil.crearRespuestaExitosa(
                    MODULO,
                    "Existencia consultada correctamente en la replica.",
                    servicio.consultarExistenciaMedicamento(clave).toHashtable()
            );
        } catch (ErrorAplicacion errorAplicacion) {
            return RespuestaRpcUtil.crearRespuestaError(MODULO, errorAplicacion);
        } catch (Exception excepcion) {
            return RespuestaRpcUtil.crearRespuestaErrorInesperado(MODULO, excepcion);
        }
    }

    public Hashtable listarMedicamentosBajoMinimo() {
        try {
            List<MedicamentoInventario> medicamentos = servicio.listarMedicamentosBajoMinimo();
            Vector<Hashtable> datos = new Vector<>();

            for (MedicamentoInventario medicamento : medicamentos) {
                datos.addElement(medicamento.toHashtable());
            }

            return RespuestaRpcUtil.crearRespuestaExitosa(
                    MODULO,
                    "Listado generado correctamente en la replica.",
                    datos
            );
        } catch (ErrorAplicacion errorAplicacion) {
            return RespuestaRpcUtil.crearRespuestaError(MODULO, errorAplicacion);
        } catch (Exception excepcion) {
            return RespuestaRpcUtil.crearRespuestaErrorInesperado(MODULO, excepcion);
        }
    }

    public Hashtable registrarEntradaMedicamento(String clave, int cantidad, String responsable) {
        try {
            ResultadoOperacionReplica resultado = servicio.registrarEntradaMedicamento(clave, cantidad, responsable);

            return RespuestaRpcUtil.crearRespuestaExitosaPersonalizada(
                    MODULO,
                    resultado.getCodigo(),
                    resultado.getMensaje(),
                    resultado.toHashtable()
            );
        } catch (ErrorAplicacion errorAplicacion) {
            return RespuestaRpcUtil.crearRespuestaError(MODULO, errorAplicacion);
        } catch (Exception excepcion) {
            return RespuestaRpcUtil.crearRespuestaErrorInesperado(MODULO, excepcion);
        }
    }

    /**
     * Metodo reservado para la sincronizacion enviada desde el principal.
     *
     * @param idOperacion identificador idempotente de la operacion.
     * @param clave clave del medicamento.
     * @param cantidad cantidad a sumar.
     * @param responsable actor registrado en auditoria.
     * @return respuesta uniforme para el principal.
     */
    public Hashtable registrarEntradaMedicamentoReplicada(String idOperacion, String clave,
            int cantidad, String responsable) {
        try {
            return RespuestaRpcUtil.crearRespuestaExitosa(
                    MODULO,
                    "Replica sincronizada correctamente.",
                    servicio.registrarEntradaMedicamentoReplicada(idOperacion, clave, cantidad, responsable).toHashtable()
            );
        } catch (ErrorAplicacion errorAplicacion) {
            return RespuestaRpcUtil.crearRespuestaError(MODULO, errorAplicacion);
        } catch (Exception excepcion) {
            return RespuestaRpcUtil.crearRespuestaErrorInesperado(MODULO, excepcion);
        }
    }

    /**
     * Devuelve al principal la cola que la replica conserva en contingencia.
     *
     * @return operaciones pendientes hacia el principal.
     */
    public Hashtable obtenerOperacionesPendientesHaciaPrincipal() {
        try {
            List<OperacionPendienteInventario> pendientes = servicio.obtenerOperacionesPendientesHaciaPrincipal();
            Vector<Hashtable> datos = new Vector<>();

            for (OperacionPendienteInventario operacion : pendientes) {
                datos.addElement(operacion.toHashtable());
            }

            return RespuestaRpcUtil.crearRespuestaExitosa(
                    MODULO,
                    "Cola de pendientes hacia el principal generada correctamente.",
                    datos
            );
        } catch (ErrorAplicacion errorAplicacion) {
            return RespuestaRpcUtil.crearRespuestaError(MODULO, errorAplicacion);
        } catch (Exception excepcion) {
            return RespuestaRpcUtil.crearRespuestaErrorInesperado(MODULO, excepcion);
        }
    }

    /**
     * Elimina de la cola una operacion que el principal ya absorbio.
     *
     * @param idOperacion identificador confirmado por el principal.
     * @return respuesta de confirmacion.
     */
    public Hashtable confirmarOperacionPendienteHaciaPrincipal(String idOperacion) {
        try {
            servicio.confirmarOperacionPendienteHaciaPrincipal(idOperacion);
            return RespuestaRpcUtil.crearRespuestaExitosa(
                    MODULO,
                    "Operacion pendiente marcada como sincronizada con el principal.",
                    new Hashtable()
            );
        } catch (ErrorAplicacion errorAplicacion) {
            return RespuestaRpcUtil.crearRespuestaError(MODULO, errorAplicacion);
        } catch (Exception excepcion) {
            return RespuestaRpcUtil.crearRespuestaErrorInesperado(MODULO, excepcion);
        }
    }
}
