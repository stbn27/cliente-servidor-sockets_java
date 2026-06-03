package servidor.modelo;

/**
 * Representa una pelicula disponible.
 */
public class Pelicula {
    private final String idPelicula;
    private final String titulo;
    private final String clasificacion;
    private final int duracionMinutos;

    /**
     * Crea una pelicula.
     *
     * @param idPelicula identificador.
     * @param titulo titulo.
     * @param clasificacion clasificacion.
     * @param duracionMinutos duracion en minutos.
     */
    public Pelicula(String idPelicula, String titulo, String clasificacion, int duracionMinutos) {
        this.idPelicula = idPelicula;
        this.titulo = titulo;
        this.clasificacion = clasificacion;
        this.duracionMinutos = duracionMinutos;
    }

    /**
     * @return id de la pelicula.
     */
    public String getIdPelicula() {
        return idPelicula;
    }

    /**
     * @return titulo de la pelicula.
     */
    public String getTitulo() {
        return titulo;
    }

    /**
     * @return clasificacion de la pelicula.
     */
    public String getClasificacion() {
        return clasificacion;
    }

    /**
     * @return duracion en minutos.
     */
    public int getDuracionMinutos() {
        return duracionMinutos;
    }
}

