package servidor.replicacion;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;
import org.apache.xmlrpc.XmlRpcClient;
import org.apache.xmlrpc.XmlRpcException;
import servidor.excepciones.CodigosError;
import servidor.excepciones.ErrorReplicaNoDisponible;
import servidor.modelos.OperacionPendienteInventario;

/**
 * Cliente XML-RPC usado por el servidor principal para sincronizar cambios
 * hacia la replica.
 */
public final class ClienteReplicaXmlRpc {

    private static final String MODULO = "REPLICACION";

    private final String hostReplica;
    private final int puertoReplica;

    public ClienteReplicaXmlRpc(String hostReplica, int puertoReplica) {
        this.hostReplica = hostReplica;
        this.puertoReplica = puertoReplica;
    }

    /**
     * Replica una entrada de inventario en el servidor replica.
     *
     * @param clave clave del medicamento.
     * @param cantidad cantidad a sumar.
     * @param responsable usuario que solicito la operacion.
     * @throws ErrorReplicaNoDisponible si la replica no responde o reporta
     * error.
     */
    public void replicarEntradaMedicamento(String idOperacion, String clave, int cantidad, String responsable)
            throws ErrorReplicaNoDisponible {
        try {
            XmlRpcClient cliente = new XmlRpcClient(hostReplica, puertoReplica);
            Vector<Object> parametros = new Vector<>();
            parametros.addElement(idOperacion);
            parametros.addElement(clave);
            parametros.addElement(cantidad);
            parametros.addElement(responsable);

            Object respuestaCruda = cliente.execute(
                    "InventarioClinico.registrarEntradaMedicamentoReplicada",
                    parametros
            );

            if (!(respuestaCruda instanceof Hashtable)) {
                throw new ErrorReplicaNoDisponible(
                        "La replica respondio con un formato inesperado.",
                        "Tipo devuelto por replica: "
                        + (respuestaCruda == null ? "null" : respuestaCruda.getClass().getName()),
                        MODULO);
            }

            Hashtable<String, Object> respuesta = (Hashtable<String, Object>) respuestaCruda;
            Boolean exito = (Boolean) respuesta.get("exito");

            if (exito == null || !exito) {
                String codigo = (String) respuesta.get("codigo");
                String mensaje = (String) respuesta.get("mensaje");
                String detalleTecnico = (String) respuesta.get("detalleTecnico");
                throw new ErrorReplicaNoDisponible(
                        mensaje == null ? "La replica rechazo la operacion." : mensaje,
                        "Codigo replica: " + codigo + " | Detalle: " + detalleTecnico,
                        MODULO);
            }
        } catch (MalformedURLException excepcion) {
            throw new ErrorReplicaNoDisponible(
                    "La ruta de la replica es invalida.",
                    excepcion.getMessage(),
                    MODULO);
        } catch (XmlRpcException excepcion) {
            throw new ErrorReplicaNoDisponible(
                    "La replica reporto un error RPC.",
                    excepcion.getMessage(),
                    MODULO);
        } catch (IOException excepcion) {
            throw new ErrorReplicaNoDisponible(
                    "No fue posible comunicar el cambio al servidor replica.",
                    excepcion.getMessage(),
                    MODULO);
        } catch (ErrorReplicaNoDisponible errorReplicaNoDisponible) {
            throw errorReplicaNoDisponible;
        } catch (Exception excepcion) {
            throw new ErrorReplicaNoDisponible(
                    "Ocurrio un error inesperado al replicar el cambio.",
                    CodigosError.ERROR_EN_EL_SERVIDOR + " | " + excepcion.getMessage(),
                    MODULO);
        }
    }

    /**
     * Obtiene los cambios que la replica aun no ha podido entregar al
     * principal.
     *
     * @return lista tipada de operaciones pendientes.
     * @throws ErrorReplicaNoDisponible si la replica no puede responder.
     */
    public List<OperacionPendienteInventario> obtenerOperacionesPendientesHaciaPrincipal()
            throws ErrorReplicaNoDisponible {

        Hashtable<String, Object> respuesta = ejecutarConRespuesta(
                "InventarioClinico.obtenerOperacionesPendientesHaciaPrincipal",
                new Vector<String>()
        );
        Object datos = respuesta.get("datos");
        List<OperacionPendienteInventario> operaciones = new ArrayList<>();

        if (datos instanceof Vector) {
            Vector<Hashtable<String, Object>> vector = (Vector<Hashtable<String, Object>>) datos;
            for (int indice = 0; indice < vector.size(); indice++) {
                Object elemento = vector.elementAt(indice);
                if (elemento != null) {
                    operaciones.add(OperacionPendienteInventario.desdeHashtable((Hashtable<String, Object>) elemento));
                }
            }
        }
        return operaciones;
    }

    /**
     * Confirma a la replica que una operacion ya fue absorbida por el
     * principal.
     *
     * @param idOperacion identificador confirmado.
     * @throws ErrorReplicaNoDisponible si la replica no puede actualizar su cola.
     */
    public void confirmarOperacionPendienteHaciaPrincipal(String idOperacion) throws ErrorReplicaNoDisponible {
        Vector<String> parametros = new Vector<>();
        parametros.addElement(idOperacion);
        ejecutarConRespuesta(
                "InventarioClinico.confirmarOperacionPendienteHaciaPrincipal",
                parametros);
    }

    private Hashtable<String, Object> ejecutarConRespuesta(String metodo, Vector<String> parametros) throws ErrorReplicaNoDisponible {
        try {
            XmlRpcClient cliente = new XmlRpcClient(hostReplica, puertoReplica);
            Object respuestaCruda = cliente.execute(metodo, parametros);
            if (!(respuestaCruda instanceof Hashtable)) {
                throw new ErrorReplicaNoDisponible(
                        "La replica respondio con un formato inesperado.",
                        "Tipo devuelto por replica: "
                        + (respuestaCruda == null ? "null" : respuestaCruda.getClass().getName()),
                        MODULO);
            }

            Hashtable<String, Object> respuesta = (Hashtable<String, Object>) respuestaCruda;
            Boolean exito = (Boolean) respuesta.get("exito");
            if (exito == null || !exito) {
                String codigo = (String) respuesta.get("codigo");
                String mensaje = (String) respuesta.get("mensaje");
                String detalleTecnico = (String) respuesta.get("detalleTecnico");

                throw new ErrorReplicaNoDisponible(
                        mensaje == null ? "La replica rechazo la operacion." : mensaje,
                        "Codigo replica: " + codigo + " | Detalle: " + detalleTecnico,
                        MODULO
                );
            }
            return respuesta;
        } catch (MalformedURLException excepcion) {
            throw new ErrorReplicaNoDisponible(
                    "La ruta de la replica es invalida.",
                    excepcion.getMessage(),
                    MODULO);
        } catch (XmlRpcException excepcion) {
            throw new ErrorReplicaNoDisponible(
                    "La replica reporto un error RPC.",
                    excepcion.getMessage(),
                    MODULO);
        } catch (IOException excepcion) {
            throw new ErrorReplicaNoDisponible(
                    "No fue posible comunicar el cambio al servidor replica.",
                    excepcion.getMessage(),
                    MODULO);
        } catch (ErrorReplicaNoDisponible errorReplicaNoDisponible) {
            throw errorReplicaNoDisponible;
        } catch (Exception excepcion) {
            throw new ErrorReplicaNoDisponible(
                    "Ocurrio un error inesperado durante la comunicacion con la replica.",
                    CodigosError.ERROR_EN_EL_SERVIDOR + " | " + excepcion.getMessage(),
                    MODULO);
        }
    }
}
