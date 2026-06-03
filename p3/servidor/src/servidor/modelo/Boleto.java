package servidor.modelo;

/**
 * Representa un boleto vendido.
 */
public class Boleto {

    private final String idBoleto;
    private final String idFuncion;
    private final String asiento;
    private final String nombreCliente;
    private final String fechaCompra;
    private final String horaCompra;

    /**
     * Crea un boleto.
     *
     * @param idBoleto identificador del boleto.
     * @param idFuncion identificador de la funcion.
     * @param asiento asiento vendido.
     * @param nombreCliente nombre del cliente.
     * @param fechaCompra fecha de compra.
     * @param horaCompra hora de compra.
     */
    public Boleto(String idBoleto, String idFuncion, String asiento, String nombreCliente, String fechaCompra, String horaCompra) {
        this.idBoleto = idBoleto;
        this.idFuncion = idFuncion;
        this.asiento = asiento;
        this.nombreCliente = nombreCliente;
        this.fechaCompra = fechaCompra;
        this.horaCompra = horaCompra;
    }

    /**
     * @return id del boleto.
     */
    public String getIdBoleto() {
        return idBoleto;
    }

    /**
     * @return id de la funcion.
     */
    public String getIdFuncion() {
        return idFuncion;
    }

    /**
     * @return asiento vendido.
     */
    public String getAsiento() {
        return asiento;
    }

    /**
     * @return nombre del cliente.
     */
    public String getNombreCliente() {
        return nombreCliente;
    }

    /**
     * @return fecha de compra.
     */
    public String getFechaCompra() {
        return fechaCompra;
    }

    /**
     * @return hora de compra.
     */
    public String getHoraCompra() {
        return horaCompra;
    }
}

