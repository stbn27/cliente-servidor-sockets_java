package servidor.modelo;

/**
 * Representa un asiento dentro de una funcion.
 */
public class Asiento {
    private final String idFuncion;
    private final String numero;
    private EstadoAsiento estado;

    /**
     * Crea un asiento.
     *
     * @param idFuncion id de la funcion.
     * @param numero identificador del asiento.
     * @param estado estado actual.
     */
    public Asiento(String idFuncion, String numero, EstadoAsiento estado) {
        this.idFuncion = idFuncion;
        this.numero = numero;
        this.estado = estado;
    }

    /**
     * @return id de la funcion.
     */
    public String getIdFuncion() {
        return idFuncion;
    }

    /**
     * @return identificador del asiento.
     */
    public String getNumero() {
        return numero;
    }

    /**
     * @return estado actual del asiento.
     */
    public EstadoAsiento getEstado() {
        return estado;
    }

    /**
     * Actualiza el estado del asiento.
     *
     * @param estado nuevo estado.
     */
    public void setEstado(EstadoAsiento estado) {
        this.estado = estado;
    }
}

