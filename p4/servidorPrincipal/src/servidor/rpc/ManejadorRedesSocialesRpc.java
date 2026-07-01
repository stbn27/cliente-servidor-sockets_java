package servidor.rpc;

import java.util.Hashtable;
import servidor.excepciones.ErrorAplicacion;
import servidor.servicios.ServicioRedesSociales;
import servidor.util.RespuestaRpcUtil;

/**
 * Handler XML-RPC para redes sociales.
 */
public final class ManejadorRedesSocialesRpc {

    private static final String MODULO = "REDES_SOCIALES";

    private final ServicioRedesSociales servicio;

    public ManejadorRedesSocialesRpc(ServicioRedesSociales servicio) {
        this.servicio = servicio;
    }

    /**
     * Consulta tendencias por pais.
     *
     * @param pais pais solicitado.
     * @return respuesta estandarizada.
     */
    public Hashtable consultarTendenciasPorPais(String pais) {
        try {
            return RespuestaRpcUtil.crearRespuestaExitosa(
                    MODULO,
                    "Tendencias obtenidas correctamente.",
                    servicio.consultarTendenciasPorPais(pais).toHashtable()
            );
        } catch (ErrorAplicacion errorAplicacion) {
            return RespuestaRpcUtil.crearRespuestaError(MODULO, errorAplicacion);
        } catch (Exception excepcion) {
            return RespuestaRpcUtil.crearRespuestaErrorInesperado(MODULO, excepcion);
        }
    }

    /**
     * Consulta estadisticas de un usuario.
     *
     * @param usuario alias del usuario.
     * @return respuesta XML-RPC.
     */
    public Hashtable consultarEstadisticasUsuario(String usuario) {
        try {
            return RespuestaRpcUtil.crearRespuestaExitosa(
                    MODULO,
                    "Estadisticas obtenidas correctamente.",
                    servicio.consultarEstadisticasUsuario(usuario).toHashtable()
            );
        } catch (ErrorAplicacion errorAplicacion) {
            return RespuestaRpcUtil.crearRespuestaError(MODULO, errorAplicacion);
        } catch (Exception excepcion) {
            return RespuestaRpcUtil.crearRespuestaErrorInesperado(MODULO, excepcion);
        }
    }

    /**
     * Registra una nueva publicacion.
     *
     * @param usuario usuario autor.
     * @param pais pais asociado.
     * @param mensaje contenido de la publicacion.
     * @return respuesta serializable.
     */
    public Hashtable publicarNuevaPublicacion(String usuario, String pais, String mensaje) {
        try {
            return RespuestaRpcUtil.crearRespuestaExitosa(
                    MODULO,
                    "Publicacion registrada correctamente.",
                    servicio.publicarNuevaPublicacion(usuario, pais, mensaje).toHashtable()
            );
        } catch (ErrorAplicacion errorAplicacion) {
            return RespuestaRpcUtil.crearRespuestaError(MODULO, errorAplicacion);
        } catch (Exception excepcion) {
            return RespuestaRpcUtil.crearRespuestaErrorInesperado(MODULO, excepcion);
        }
    }
}
