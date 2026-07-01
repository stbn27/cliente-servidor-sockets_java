package cliente.rpc;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Hashtable;
import java.util.Vector;
import org.apache.xmlrpc.XmlRpcClient;
import org.apache.xmlrpc.XmlRpcException;

/**
 * Encapsula las invocaciones XML-RPC del cliente.
 */
public final class ClienteRpc {

    private static final String MODULO = "CLIENTE_RPC";

    private final String hostPrincipal;
    private final int puertoPrincipal;
    private final String hostReplica;
    private final int puertoReplica;

    public ClienteRpc(String hostPrincipal, int puertoPrincipal, String hostReplica, int puertoReplica) {
        this.hostPrincipal = hostPrincipal;
        this.puertoPrincipal = puertoPrincipal;
        this.hostReplica = hostReplica;
        this.puertoReplica = puertoReplica;
    }

    /**
     * Invoca un metodo remoto y normaliza los errores de comunicacion.
     *
     * @param metodo nombre completo del metodo XML-RPC.
     * @param parametros parametros enviados en Vector.
     * @return hashtable uniforme para el menu de consola.
     */
    public Hashtable invocar(String metodo, Vector parametros) {

        try {
            return ejecutarContraServidor(hostPrincipal, puertoPrincipal, metodo, parametros, "PRINCIPAL");
        } catch (MalformedURLException excepcion) {
            return crearError(
                    "400_PARAMETRO_INVALIDO",
                    "La direccion del servidor principal no es valida.",
                    excepcion.getMessage(),
                    MODULO
            );
        } catch (XmlRpcException excepcion) {
            return crearError(
                    "404_METODO_NO_ENCONTRADO",
                    "El servidor no pudo resolver la operacion solicitada.",
                    excepcion.getMessage(),
                    MODULO
            );
        } catch (IOException excepcion) {

            if (esMetodoDeInventarioClinico(metodo)) {
                return intentarReplicaEnContingencia(metodo, parametros, excepcion);
            }

            return crearError(
                    "503_SERVIDOR_PRINCIPAL_NO_DISPONIBLE",
                    "No fue posible comunicar con el servidor principal.",
                    excepcion.getLocalizedMessage(),
                    MODULO
            );
        } catch (Exception excepcion) {
            return crearError(
                    "500_ERROR_EN_EL_SERVIDOR",
                    "Ocurrio un error inesperado al invocar el servicio remoto.",
                    excepcion.getClass().getName() + ": " + excepcion.getMessage(),
                    MODULO
            );
        }
    }

    /**
     * Intenta ejecutar una operación contra un servidor réplica en caso de que el servidor principal
     * no esté disponible, manejando diversos errores de comunicación que puedan ocurrir.
     *
     * @param metodo el nombre completo del método XML-RPC a ejecutar.
     * @param parametros los parámetros enviados en un objeto Vector.
     * @param errorPrincipal excepción de tipo IOException que describe el error ocurrido al intentar
     *                        comunicarse con el servidor principal.
     * @return un objeto Hashtable que contiene la respuesta del servidor réplica o la información
     *         de error generada en caso de que la comunicación falle.
     */
    private Hashtable intentarReplicaEnContingencia(String metodo, Vector parametros, IOException errorPrincipal) {

        try {
            Hashtable respuestaReplica = ejecutarContraServidor(
                    hostReplica,
                    puertoReplica,
                    metodo,
                    parametros,
                    "REPLICA_CONTINGENCIA"
            );
            String detallePrevio = "El servidor principal no estuvo disponible: " + errorPrincipal.getLocalizedMessage() + ". Operacion atendida por la replica.";
            Object detalleActual = respuestaReplica.get("detalleTecnico");

            if (detalleActual == null || String.valueOf(detalleActual).trim().isEmpty()) {
                respuestaReplica.put("detalleTecnico", detallePrevio);
            } else {
                respuestaReplica.put("detalleTecnico", detallePrevio + " | " + detalleActual);
            }

            respuestaReplica.put("origenAtencion", "REPLICA_CONTINGENCIA");

            return respuestaReplica;
        } catch (MalformedURLException excepcion) {
            return crearError(
                    "400_PARAMETRO_INVALIDO",
                    "La direccion de la replica no es valida.",
                    excepcion.getMessage(),
                    MODULO);
        } catch (XmlRpcException excepcion) {
            return crearError(
                    "404_METODO_NO_ENCONTRADO",
                    "La replica no pudo resolver la operacion solicitada.",
                    excepcion.getMessage(),
                    MODULO);
        } catch (IOException excepcion) {
            return crearError(
                    "503_SERVIDOR_PRINCIPAL_Y_REPLICA_NO_DISPONIBLES",
                    "No fue posible comunicar ni con el servidor principal ni con la replica.",
                    "Principal: " + errorPrincipal.getLocalizedMessage() + " | Replica: " + excepcion.getLocalizedMessage(),
                    MODULO);
        } catch (Exception excepcion) {
            return crearError(
                    "500_ERROR_EN_EL_SERVIDOR",
                    "Ocurrio un error inesperado al usar la replica en contingencia.",
                    excepcion.getClass().getName() + ": " + excepcion.getMessage(),
                    MODULO);
        }
    }

    /**
     * Ejecuta un método remoto contra un servidor específico utilizando el protocolo XML-RPC
     * y maneja la respuesta recibida para devolverla en un formato estándar.
     *
     * @param host el nombre del host o dirección IP del servidor al que se desea conectar.
     * @param puerto el número de puerto en el que el servidor está escuchando.
     * @param metodo el nombre del método XML-RPC que se ejecutará en el servidor.
     * @param parametros los parámetros necesarios para la ejecución del método, empaquetados en un objeto Vector.
     * @param origen una etiqueta que identifica el origen de la solicitud, utilizada en el procesamiento de la respuesta.
     * @return un objeto Hashtable que contiene la respuesta del servidor si la operación es exitosa.
     *         Si el servidor responde en un formato inesperado, devuelve un objeto Hashtable con un error estandarizado.
     * @throws MalformedURLException si la URL del servidor no es válida.
     * @throws XmlRpcException si ocurre un error durante la comunicación XML-RPC.
     * @throws IOException si ocurre un error de E/S durante la conexión al servidor.
     */
    private Hashtable ejecutarContraServidor(String host, int puerto, String metodo, Vector parametros, String origen)
            throws MalformedURLException, XmlRpcException, IOException {

        XmlRpcClient cliente = new XmlRpcClient(host, puerto);
        Object respuestaCruda = cliente.execute(metodo, parametros);

        if (respuestaCruda instanceof Hashtable) {
            Hashtable<String, Object> respuesta = (Hashtable<String, Object>) respuestaCruda;
            respuesta.put("origenAtencion", origen);
            return respuesta;
        }

        return crearError(
                "500_ERROR_EN_EL_SERVIDOR",
                "El servidor respondio con un formato inesperado.",
                "Tipo devuelto: " + (respuestaCruda == null ? "null" : respuestaCruda.getClass().getName()),
                MODULO
        );
    }

    private boolean esMetodoDeInventarioClinico(String metodo) {
        return metodo != null && metodo.startsWith("InventarioClinico.");
    }

    private Hashtable<String, Object> crearError(String codigo, String mensaje, String detalleTecnico, String modulo) {
        Hashtable<String, Object> respuesta = new Hashtable<>();
        respuesta.put("exito", Boolean.FALSE);
        respuesta.put("codigo", codigo);
        respuesta.put("mensaje", mensaje);
        respuesta.put("detalleTecnico", detalleTecnico);
        respuesta.put("modulo", modulo);
        respuesta.put("origenAtencion", "SIN_ATENCION");
        respuesta.put("datos", new Hashtable());
        return respuesta;
    }
}
