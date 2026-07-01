package servidor.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Hashtable;
import servidor.excepciones.CodigosError;
import servidor.excepciones.ErrorAplicacion;

/**
 * Fabrica respuestas XML-RPC de la replica.
 */
public final class RespuestaRpcUtil {

    private static final DateTimeFormatter FORMATO = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private RespuestaRpcUtil() {
    }

    public static Hashtable crearRespuestaExitosa(String modulo, String mensaje, Object datos) {
        return crearRespuesta(Boolean.TRUE, CodigosError.OK, mensaje, "", modulo, datos);
    }

    public static Hashtable crearRespuestaExitosaPersonalizada(
            String modulo, String codigo, String mensaje, Object datos) {
        return crearRespuesta(Boolean.TRUE, codigo, mensaje, "", modulo, datos);
    }

    public static Hashtable crearRespuestaError(String modulo, ErrorAplicacion errorAplicacion) {
        return crearRespuesta(Boolean.FALSE,
                errorAplicacion.getCodigo(),
                errorAplicacion.getMessage(),
                errorAplicacion.getDetalleTecnico(),
                modulo,
                new Hashtable()
        );
    }

    public static Hashtable crearRespuestaErrorInesperado(String modulo, Exception excepcion) {
        return crearRespuesta(Boolean.FALSE,
                CodigosError.ERROR_EN_EL_SERVIDOR,
                "Ocurrio un error interno en la replica.",
                excepcion.getClass().getName() + ": " + excepcion.getMessage(),
                modulo,
                new Hashtable()
        );
    }

    private static Hashtable<String, Object> crearRespuesta(Boolean exito, String codigo, String mensaje,
            String detalleTecnico, String modulo, Object datos) {
        Hashtable<String, Object> respuesta = new Hashtable<>();

        respuesta.put("exito", exito);
        respuesta.put("codigo", codigo);
        respuesta.put("mensaje", mensaje);
        respuesta.put("detalleTecnico", detalleTecnico == null ? "" : detalleTecnico);
        respuesta.put("modulo", modulo);
        respuesta.put("marcaTiempo", LocalDateTime.now().format(FORMATO));
        respuesta.put("datos", datos == null ? new Hashtable<String, Object>() : datos);

        return respuesta;
    }
}
