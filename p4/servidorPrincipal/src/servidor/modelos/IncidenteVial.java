package servidor.modelos;

import java.util.Hashtable;

/**
 * Reporte de incidente vial levantado por consola.
 */
public final class IncidenteVial {

    private final int identificador;
    private final String zona;
    private final String tipo;
    private final String descripcion;
    private final String fechaHora;
    private final String severidad;
    private final String reportadoPor;

    public IncidenteVial(int identificador, String zona, String tipo, String descripcion,
            String fechaHora, String severidad, String reportadoPor) {
        this.identificador = identificador;
        this.zona = zona;
        this.tipo = tipo;
        this.descripcion = descripcion;
        this.fechaHora = fechaHora;
        this.severidad = severidad;
        this.reportadoPor = reportadoPor;
    }

    public int getIdentificador() {
        return identificador;
    }

    public Hashtable<String, Object> toHashtable() {

        Hashtable<String, Object> tabla = new Hashtable<>();

        tabla.put("identificador", identificador);
        tabla.put("zona", zona);
        tabla.put("tipo", tipo);
        tabla.put("descripcion", descripcion);
        tabla.put("fechaHora", fechaHora);
        tabla.put("severidad", severidad);
        tabla.put("reportadoPor", reportadoPor);

        return tabla;
    }
}
