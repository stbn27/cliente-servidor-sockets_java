package servidor.modelos;

import java.util.Hashtable;

/**
 * Resume el resultado de una escritura atendida directamente por la replica.
 */
public final class ResultadoOperacionReplica {

    private final String codigo;
    private final String mensaje;
    private final String idOperacion;
    private final MedicamentoInventario medicamento;
    private final boolean principalSincronizado;
    private final String detallePrincipal;

    /**
     * Crea el resultado de una escritura hecha en contingencia.
     *
     * @param codigo codigo resumido del resultado.
     * @param mensaje mensaje legible.
     * @param idOperacion identificador idempotente.
     * @param medicamento medicamento actualizado.
     * @param principalSincronizado indica si el principal ya absorbio el cambio.
     * @param detallePrincipal detalle de sincronizacion hacia el principal.
     */
    public ResultadoOperacionReplica(String codigo, String mensaje, String idOperacion,
            MedicamentoInventario medicamento, boolean principalSincronizado, String detallePrincipal) {
        this.codigo = codigo;
        this.mensaje = mensaje;
        this.idOperacion = idOperacion;
        this.medicamento = medicamento;
        this.principalSincronizado = principalSincronizado;
        this.detallePrincipal = detallePrincipal;
    }

    public String getCodigo() {
        return codigo;
    }

    public String getMensaje() {
        return mensaje;
    }

    public Hashtable<String, Object> toHashtable() {

        Hashtable<String, Object> tabla = new Hashtable<>();

        tabla.put("codigo", codigo);
        tabla.put("mensaje", mensaje);
        tabla.put("idOperacion", idOperacion);
        tabla.put("principalSincronizado", principalSincronizado);
        tabla.put("detallePrincipal", detallePrincipal);
        tabla.put("medicamento", medicamento.toHashtable());

        return tabla;
    }
}
