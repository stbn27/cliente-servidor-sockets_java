package servidor.replicacion;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Hashtable;
import java.util.Vector;
import org.apache.xmlrpc.XmlRpcClient;
import org.apache.xmlrpc.XmlRpcException;
import servidor.excepciones.CodigosError;
import servidor.excepciones.ErrorAplicacion;
import servidor.modelos.OperacionPendienteInventario;

/**
 * Cliente XML-RPC que usa la replica para entregar cambios pendientes al
 * servidor principal.
 */
public final class ClientePrincipalXmlRpc {

    private static final String MODULO = "SINCRONIZACION_REPLICA";

    private final String hostPrincipal;
    private final int puertoPrincipal;

    /**
     * Crea el cliente de sincronizacion hacia el principal.
     *
     * @param hostPrincipal host del principal.
     * @param puertoPrincipal puerto del principal.
     */
    public ClientePrincipalXmlRpc(String hostPrincipal, int puertoPrincipal) {
        this.hostPrincipal = hostPrincipal;
        this.puertoPrincipal = puertoPrincipal;
    }

    /**
     * Entrega al principal una operacion originada en contingencia.
     *
     * @param operacion operacion local pendiente.
     * @throws ErrorAplicacion si el principal no responde o rechaza la operacion.
     */
    public void enviarOperacionPendiente(OperacionPendienteInventario operacion) throws ErrorAplicacion {
        try {
            XmlRpcClient cliente = new XmlRpcClient(hostPrincipal, puertoPrincipal);
            Vector<Object> parametros = new Vector<>();
            parametros.addElement(operacion.idOperacion());
            parametros.addElement(operacion.clave());
            parametros.addElement(operacion.cantidad());
            parametros.addElement(operacion.responsable());

            Object respuestaCruda = cliente.execute(
                    "InventarioClinico.registrarEntradaMedicamentoDesdeReplica",
                    parametros
            );

            if (!(respuestaCruda instanceof Hashtable)) {
                throw new ErrorAplicacion(
                        CodigosError.ERROR_EN_EL_SERVIDOR,
                        "El principal respondio con un formato inesperado.",
                        "Tipo devuelto: "
                        + (respuestaCruda == null ? "null" : respuestaCruda.getClass().getName()),
                        MODULO
                );
            }

            Hashtable respuesta = (Hashtable) respuestaCruda;
            Boolean exito = (Boolean) respuesta.get("exito");

            if (exito == null || !exito) {
                throw new ErrorAplicacion(
                        String.valueOf(respuesta.get("codigo")),
                        String.valueOf(respuesta.get("mensaje")),
                        String.valueOf(respuesta.get("detalleTecnico")),
                        MODULO
                );
            }

        } catch (MalformedURLException excepcion) {
            throw new ErrorAplicacion(
                    CodigosError.PARAMETRO_INVALIDO,
                    "La ruta del principal es invalida.",
                    excepcion.getMessage(),
                    MODULO
            );
        } catch (XmlRpcException excepcion) {
            throw new ErrorAplicacion(
                    CodigosError.ERROR_EN_EL_SERVIDOR,
                    "El principal reporto un error RPC.",
                    excepcion.getMessage(),
                    MODULO
            );
        } catch (IOException excepcion) {
            throw new ErrorAplicacion(
                    CodigosError.ERROR_EN_EL_SERVIDOR,
                    "No fue posible comunicar la operacion al principal.",
                    excepcion.getMessage(),
                    MODULO
            );
        }
    }
}
