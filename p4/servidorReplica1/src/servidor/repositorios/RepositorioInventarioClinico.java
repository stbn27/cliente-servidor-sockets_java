package servidor.repositorios;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import servidor.excepciones.ErrorNoEncontrado;
import servidor.excepciones.ErrorPersistencia;
import servidor.modelos.MedicamentoInventario;
import servidor.persistencia.ArchivoCsvUtil;

/**
 * Repositorio local del inventario clinico replicado.
 */
public final class RepositorioInventarioClinico {

    private static final String MODULO = "INVENTARIO_CLINICO_REPLICA";

    private final Path archivoMedicamentos;

    public RepositorioInventarioClinico(Path directorioBase) {
        this.archivoMedicamentos = directorioBase.resolve("medicamentos.csv");
    }

    public MedicamentoInventario buscarPorClave(String clave) throws ErrorNoEncontrado, ErrorPersistencia {
        List<MedicamentoInventario> medicamentos = leerMedicamentos();

        for (MedicamentoInventario medicamento : medicamentos) {
            if (medicamento.clave().equalsIgnoreCase(clave)) {
                return medicamento;
            }
        }

        throw new ErrorNoEncontrado(
                "La clave solicitada no existe en la replica.",
                "Clave inexistente: " + clave,
                MODULO
        );
    }

    public List<MedicamentoInventario> listarBajoMinimo() throws ErrorPersistencia {
        List<MedicamentoInventario> resultado = new ArrayList<>();
        List<MedicamentoInventario> medicamentos = leerMedicamentos();

        for (MedicamentoInventario medicamento : medicamentos) {
            if (medicamento.estaBajoMinimo()) {
                resultado.add(medicamento);
            }
        }

        return resultado;
    }

    public synchronized MedicamentoInventario registrarEntrada(String clave, int cantidad)
            throws ErrorNoEncontrado, ErrorPersistencia {
        List<MedicamentoInventario> medicamentos = leerMedicamentos();
        List<MedicamentoInventario> actualizados = new ArrayList<>();
        MedicamentoInventario medicamentoActualizado = null;

        for (MedicamentoInventario medicamento : medicamentos) {
            if (medicamento.clave().equalsIgnoreCase(clave)) {
                medicamentoActualizado = medicamento.sumarExistencias(cantidad);
                actualizados.add(medicamentoActualizado);
            } else {
                actualizados.add(medicamento);
            }
        }

        if (medicamentoActualizado == null) {
            throw new ErrorNoEncontrado(
                    "No fue posible aplicar la entrada en la replica porque la clave no existe.",
                    "Clave inexistente: " + clave,
                    MODULO
            );
        }

        guardarMedicamentos(actualizados);

        return medicamentoActualizado;
    }

    private List<MedicamentoInventario> leerMedicamentos() throws ErrorPersistencia {
        List<String[]> registros = ArchivoCsvUtil.leerRegistros(archivoMedicamentos, MODULO);
        List<MedicamentoInventario> medicamentos = new ArrayList<>();

        for (String[] columnas : registros) {
            medicamentos.add(new MedicamentoInventario(
                    columnas[0],
                    columnas[1],
                    Integer.parseInt(columnas[2]),
                    Integer.parseInt(columnas[3]),
                    columnas[4],
                    columnas[5])
            );
        }

        return medicamentos;
    }

    private void guardarMedicamentos(List<MedicamentoInventario> medicamentos) throws ErrorPersistencia {
        List<String> lineas = new ArrayList<>();
        lineas.add("clave|nombre|existencias|minimoRecomendado|ubicacion|proveedor");

        for (MedicamentoInventario medicamento : medicamentos) {
            lineas.add(medicamento.clave() + "|"
                    + medicamento.nombre() + "|"
                    + medicamento.existencias() + "|"
                    + medicamento.minimoRecomendado() + "|"
                    + medicamento.ubicacion() + "|"
                    + medicamento.proveedor()
            );
        }
        ArchivoCsvUtil.escribirArchivo(archivoMedicamentos, lineas, MODULO);
    }
}
