package servidor.modelos;

import java.util.Hashtable;

/**
 * Modela una operacion de inventario pendiente de sincronizar con el otro
 * nodo.
 */
public record OperacionPendienteInventario(String idOperacion, String clave, int cantidad, String responsable,
                                           String marcaTiempo, String origen) {

    /**
     * Crea una operacion pendiente o aplicada.
     *
     * @param idOperacion identificador idempotente.
     * @param clave       clave del medicamento.
     * @param cantidad    cantidad a sumar.
     * @param responsable actor de la operacion.
     * @param marcaTiempo fecha de creacion.
     * @param origen      nodo que origino el cambio.
     */
    public OperacionPendienteInventario {
    }

    /**
     * Convierte la operacion en una estructura XML-RPC.
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
}
