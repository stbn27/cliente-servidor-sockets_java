package servidor.modelos;

import java.util.Hashtable;

/**
 * Representa la disponibilidad de bicicletas en una estacion.
 */
public final class EstacionEcoBici {

    private final String nombre;
    private final int bicicletasDisponibles;
    private final int anclajesLibres;
    private final String estado;

    public EstacionEcoBici(String nombre, int bicicletasDisponibles, int anclajesLibres, String estado) {
        this.nombre = nombre;
        this.bicicletasDisponibles = bicicletasDisponibles;
        this.anclajesLibres = anclajesLibres;
        this.estado = estado;
    }

    public String getNombre() {
        return nombre;
    }

    public Hashtable<String, Object> toHashtable() {
        Hashtable<String, Object> tabla = new Hashtable<>();

        tabla.put("estacion", nombre);
        tabla.put("bicicletasDisponibles", bicicletasDisponibles);
        tabla.put("anclajesLibres", anclajesLibres);
        tabla.put("estado", estado);
        return tabla;
    }
}
