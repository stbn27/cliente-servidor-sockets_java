package servidor.modelos;

import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

/**
 * Modela las tendencias de redes sociales para un pais.
 */
public record TendenciaPais(String pais, List<String> tendencias) {

    /**
     * Convierte el modelo a una estructura simple compatible con XML-RPC.
     *
     * @return hashtable serializable por la libreria clasica.
     */
    public Hashtable<String, Object> toHashtable() {

        Hashtable<String, Object> tabla = new Hashtable<>();

        tabla.put("pais", pais);
        tabla.put("tendencias", new Vector<>(tendencias));

        return tabla;
    }
}
