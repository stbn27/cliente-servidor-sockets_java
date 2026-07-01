package servidor.rpc;

import java.util.Hashtable;
import java.util.List;
import java.util.Vector;
import servidor.excepciones.ErrorAplicacion;
import servidor.modelos.MedicamentoInventario;
import servidor.modelos.ResultadoReplicacionInventario;
import servidor.servicios.ServicioInventarioClinico;
import servidor.util.RespuestaRpcUtil;

/**
 * Handler XML-RPC para el dominio replicado: inventario clinico.
 */
public final class ManejadorInventarioClinicoRpc {

    private static final String MODULO = "INVENTARIO_CLINICO";

    private final ServicioInventarioClinico servicio;

    public ManejadorInventarioClinicoRpc(ServicioInventarioClinico servicio) {
        this.servicio = servicio;
    }

    public Hashtable<String, Object> consultarExistenciaMedicamento(String clave) {
        try {
            return RespuestaRpcUtil.crearRespuestaExitosa(
                    MODULO,
                    "Existencia obtenida correctamente.",
                    servicio.consultarExistenciaMedicamento(clave).toHashtable()
            );
        } catch (ErrorAplicacion errorAplicacion) {
            return RespuestaRpcUtil.crearRespuestaError(MODULO, errorAplicacion);
        } catch (Exception excepcion) {
            return RespuestaRpcUtil.crearRespuestaErrorInesperado(MODULO, excepcion);
        }
    }

    public Hashtable<String, Object> listarMedicamentosBajoMinimo() {
        try {
            List<MedicamentoInventario> medicamentos = servicio.listarMedicamentosBajoMinimo();
            Vector<Hashtable<String, Object>> datos = new Vector<>();

            for (MedicamentoInventario medicamento : medicamentos) {
                datos.addElement(medicamento.toHashtable());
            }

            return RespuestaRpcUtil.crearRespuestaExitosa(
                    MODULO,
                    "Listado de medicamentos bajo minimo generado.",
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
            ResultadoReplicacionInventario resultado = servicio.registrarEntradaMedicamento(clave, cantidad, responsable);

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
     * Permite a la replica entregar al principal una operacion ejecutada en
     * modo contingencia.
     *
     * @param idOperacion identificador idempotente.
     * @param clave clave del medicamento.
     * @param cantidad cantidad a sumar.
     * @param responsable actor de la operacion.
     * @return respuesta estandarizada con el medicamento actualizado.
     */
    public Hashtable registrarEntradaMedicamentoDesdeReplica(String idOperacion,
                                                             String clave,
                                                             int cantidad,
                                                             String responsable
    ) {
        try {
            return RespuestaRpcUtil.crearRespuestaExitosa(
                    MODULO,
                    "Operacion de replica aplicada correctamente en el principal.",
                    servicio.registrarEntradaMedicamentoDesdeReplica(idOperacion, clave, cantidad, responsable).toHashtable()
            );
        } catch (ErrorAplicacion errorAplicacion) {
            return RespuestaRpcUtil.crearRespuestaError(MODULO, errorAplicacion);
        } catch (Exception excepcion) {
            return RespuestaRpcUtil.crearRespuestaErrorInesperado(MODULO, excepcion);
        }
    }
}
