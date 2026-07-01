package servidor.modelos;

import java.util.Hashtable;

/**
 * Resume el resultado de una escritura replicada.
 */
public final class ResultadoReplicacionInventario {

    private final String codigo;
    private final String mensaje;
    private final String idOperacion;
    private final MedicamentoInventario medicamento;
    private final boolean escrituraLocalExitosa;
    private final boolean replicaSincronizada;
    private final String detalleReplica;

    public ResultadoReplicacionInventario(String codigo, String mensaje, String idOperacion,
            MedicamentoInventario medicamento, boolean escrituraLocalExitosa,
            boolean replicaSincronizada, String detalleReplica) {
        this.codigo = codigo;
        this.mensaje = mensaje;
        this.idOperacion = idOperacion;
        this.medicamento = medicamento;
        this.escrituraLocalExitosa = escrituraLocalExitosa;
        this.replicaSincronizada = replicaSincronizada;
        this.detalleReplica = detalleReplica;
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
        tabla.put("escrituraLocalExitosa", escrituraLocalExitosa);
        tabla.put("replicaSincronizada", replicaSincronizada);
        tabla.put("detalleReplica", detalleReplica);
        tabla.put("medicamento", medicamento.toHashtable());

        return tabla;
    }
}
