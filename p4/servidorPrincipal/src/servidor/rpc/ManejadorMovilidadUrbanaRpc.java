package servidor.rpc;

import java.util.Hashtable;
import servidor.excepciones.ErrorAplicacion;
import servidor.servicios.ServicioMovilidadUrbana;
import servidor.util.RespuestaRpcUtil;

/**
 * Handler XML-RPC para movilidad urbana.
 */
public final class ManejadorMovilidadUrbanaRpc {

    private static final String MODULO = "MOVILIDAD_URBANA";

    private final ServicioMovilidadUrbana servicio;

    public ManejadorMovilidadUrbanaRpc(ServicioMovilidadUrbana servicio) {
        this.servicio = servicio;
    }

    public Hashtable consultarTraficoPorZona(String zona) {
        try {
            return RespuestaRpcUtil.crearRespuestaExitosa(
                    MODULO,
                    "Estado de trafico obtenido correctamente.",
                    servicio.consultarTraficoPorZona(zona).toHashtable()
            );
        } catch (ErrorAplicacion errorAplicacion) {
            return RespuestaRpcUtil.crearRespuestaError(MODULO, errorAplicacion);
        } catch (Exception excepcion) {
            return RespuestaRpcUtil.crearRespuestaErrorInesperado(MODULO, excepcion);
        }
    }

    public Hashtable consultarEcoBicicletasPorEstacion(String estacion) {
        try {
            return RespuestaRpcUtil.crearRespuestaExitosa(
                    MODULO,
                    "Disponibilidad EcoBici obtenida correctamente.",
                    servicio.consultarEcoBicicletasPorEstacion(estacion).toHashtable()
            );
        } catch (ErrorAplicacion errorAplicacion) {
            return RespuestaRpcUtil.crearRespuestaError(MODULO, errorAplicacion);
        } catch (Exception excepcion) {
            return RespuestaRpcUtil.crearRespuestaErrorInesperado(MODULO, excepcion);
        }
    }

    public Hashtable reportarIncidenteVial(String zona, String tipo, String descripcion,
            String severidad, String reportadoPor) {
        try {
            return RespuestaRpcUtil.crearRespuestaExitosa(
                    MODULO,
                    "Incidente vial registrado correctamente.",
                    servicio.reportarIncidenteVial(zona, tipo, descripcion, severidad, reportadoPor).toHashtable()
            );
        } catch (ErrorAplicacion errorAplicacion) {
            return RespuestaRpcUtil.crearRespuestaError(MODULO, errorAplicacion);
        } catch (Exception excepcion) {
            return RespuestaRpcUtil.crearRespuestaErrorInesperado(MODULO, excepcion);
        }
    }
}
