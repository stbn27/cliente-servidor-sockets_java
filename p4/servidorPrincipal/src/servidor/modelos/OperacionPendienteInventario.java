package servidor.modelos;

import java.util.Hashtable;

/**
 * Representa una operacion de inventario que aun no ha sido confirmada por el
 * otro nodo.
 *
 * <p>La misma estructura se reutiliza para:
 * <ul>
 * <li>pendientes de envio del principal hacia la replica;</li>
 * <li>pendientes de envio de la replica hacia el principal;</li>
 * <li>registro de operaciones remotas ya aplicadas para evitar duplicados.</li>
 * </ul>
 */
public record OperacionPendienteInventario(String idOperacion,
                                           String clave,
                                           int cantidad,
                                           String responsable,
                                           String marcaTiempo,
                                           String origen) {

    /**
     * Crea una operacion de sincronizacion.
     *
     * @param idOperacion identificador unico e idempotente.
     * @param clave       clave del medicamento afectado.
     * @param cantidad    cantidad sumada al inventario.
     * @param responsable actor que origino la operacion.
     * @param marcaTiempo fecha y hora de creacion.
     * @param origen      nodo que origino el cambio.
     */
    public OperacionPendienteInventario {
    }

    /**
     * Convierte la operacion a un mapa compatible con XML-RPC.
     *
     * @return tabla serializable.
     */
    public Hashtable<String, Object> toHashtable() {

        Hashtable<String, Object> tabla = new Hashtable<>();

        tabla.put("idOperacion", idOperacion);
        tabla.put("clave", clave);
        tabla.put("cantidad", cantidad);
        tabla.put("responsable", responsable);
        tabla.put("marcaTiempo", marcaTiempo);
        tabla.put("origen", origen);

        return tabla;
    }

    /**
     * Reconstruye una operacion a partir de un mapa XML-RPC.
     *
     * @param tabla estructura recibida del otro nodo.
     * @return operacion tipada.
     */
    public static OperacionPendienteInventario desdeHashtable(Hashtable tabla) {
        return new OperacionPendienteInventario(
                String.valueOf(tabla.get("idOperacion")),
                String.valueOf(tabla.get("clave")),
                Integer.parseInt(String.valueOf(tabla.get("cantidad"))),
                String.valueOf(tabla.get("responsable")),
                String.valueOf(tabla.get("marcaTiempo")),
                String.valueOf(tabla.get("origen")));
    }
}
