package servidor.modelos;

import java.util.Hashtable;

/**
 * Estado simplificado de trafico para una zona urbana.
 */
public final class TraficoZona {

    private final String zona;
    private final String nivel;
    private final int velocidadPromedio;
    private final String detalle;

    public TraficoZona(String zona, String nivel, int velocidadPromedio, String detalle) {
        this.zona = zona;
        this.nivel = nivel;
        this.velocidadPromedio = velocidadPromedio;
        this.detalle = detalle;
    }

    public String getZona() {
        return zona;
    }

    public Hashtable<String, Object> toHashtable() {

        Hashtable<String, Object> tabla = new Hashtable<>();

        tabla.put("zona", zona);
        tabla.put("nivel", nivel);
        tabla.put("velocidadPromedio", velocidadPromedio);
        tabla.put("detalle", detalle);

        return tabla;
    }
}
