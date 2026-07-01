package servidor.modelos;

import java.util.Hashtable;

/**
 * Representa una publicacion corta creada por un usuario.
 */
public record Publicacion(int identificador, String usuario, String pais, String mensaje, String fechaHora) {

    public Hashtable<String, Object> toHashtable() {

        Hashtable<String, Object> tabla = new Hashtable<>();

        tabla.put("identificador", identificador);
        tabla.put("usuario", usuario);
        tabla.put("pais", pais);
        tabla.put("mensaje", mensaje);
        tabla.put("fechaHora", fechaHora);

        return tabla;
    }
}
