package servidor.modelo;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Representa una funcion o proyeccion de cine disponible para venta.
 */
public class FuncionCine {
    private final String idFuncion;
    private final String idPelicula;
    private final String sala;
    private final String fecha;
    private final String hora;

    /**
     * Crea una funcion de cine.
     *
     * @param idFuncion identificador de la funcion.
     * @param idPelicula identificador de la pelicula.
     * @param sala sala donde se proyecta.
     * @param fecha fecha de la funcion en formato ISO.
     * @param hora hora de la funcion en formato HH:mm.
     */
    public FuncionCine(String idFuncion, String idPelicula, String sala, String fecha, String hora) {
        this.idFuncion = idFuncion;
        this.idPelicula = idPelicula;
        this.sala = sala;
        this.fecha = fecha;
        this.hora = hora;
    }

    /**
     * Obtiene el identificador de la funcion.
     *
     * @return identificador de la funcion.
     */
    public String getIdFuncion() {
        return idFuncion;
    }

    /**
     * Obtiene el identificador de la pelicula.
     *
     * @return identificador de la pelicula.
     */
    public String getIdPelicula() {
        return idPelicula;
    }

    /**
     * Obtiene la sala de proyeccion.
     *
     * @return sala de proyeccion.
     */
    public String getSala() {
        return sala;
    }

    /**
     * Obtiene la fecha en formato de texto.
     *
     * @return fecha de la funcion.
     */
    public String getFecha() {
        return fecha;
    }

    /**
     * Obtiene la hora en formato de texto.
     *
     * @return hora de la funcion.
     */
    public String getHora() {
        return hora;
    }

    /**
     * Obtiene la fecha como {@link LocalDate}.
     *
     * @return fecha convertida a {@link LocalDate}.
     */
    public LocalDate getFechaComoLocalDate() {
        return LocalDate.parse(fecha);
    }

    /**
     * Obtiene la hora como {@link LocalTime}.
     *
     * @return hora convertida a {@link LocalTime}.
     */
    public LocalTime getHoraComoLocalTime() {
        return LocalTime.parse(hora);
    }
}
