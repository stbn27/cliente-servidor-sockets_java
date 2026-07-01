package servidor.modelos;

import java.util.Hashtable;

/**
 * Registro de inventario clinico.
 */
public record MedicamentoInventario(String clave,
                                    String nombre,
                                    int existencias,
                                    int minimoRecomendado,
                                    String ubicacion,
                                    String proveedor) {

    public boolean estaBajoMinimo() {
        return existencias < minimoRecomendado;
    }

    public MedicamentoInventario sumarExistencias(int cantidad) {
        return new MedicamentoInventario(clave, nombre, existencias + cantidad, minimoRecomendado, ubicacion, proveedor);
    }

    public Hashtable<String, Object> toHashtable() {

        Hashtable<String, Object> tabla = new Hashtable<>();

        tabla.put("clave", clave);
        tabla.put("nombre", nombre);
        tabla.put("existencias", existencias);
        tabla.put("minimoRecomendado", minimoRecomendado);
        tabla.put("ubicacion", ubicacion);
        tabla.put("proveedor", proveedor);
        tabla.put("bajoMinimo", estaBajoMinimo());

        return tabla;
    }
}
