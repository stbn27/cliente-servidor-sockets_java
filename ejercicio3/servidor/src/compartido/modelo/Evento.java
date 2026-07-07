package compartido.modelo;

import java.io.Serializable;

public class Evento implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String titulo;
    private final String fecha;
    private final String hora;

    public Evento(String titulo, String fecha, String hora) {
        this.titulo = titulo;
        this.fecha = fecha;
        this.hora = hora;
    }

    public String getTitulo() {
        return titulo;
    }

    public String getFecha() {
        return fecha;
    }

    public String getHora() {
        return hora;
    }

    @Override
    public String toString() {
        return titulo + " | " + fecha + " | " + hora;
    }
}
