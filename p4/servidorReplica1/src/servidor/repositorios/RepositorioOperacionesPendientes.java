package servidor.repositorios;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import servidor.excepciones.ErrorPersistencia;
import servidor.modelos.OperacionPendienteInventario;
import servidor.persistencia.ArchivoCsvUtil;

/**
 * Persistencia local de operaciones de sincronizacion.
 */
public final class RepositorioOperacionesPendientes {

    private final String modulo;
    private final Path archivoPendientes;

    /**
     * Crea el repositorio.
     *
     * @param archivoPendientes archivo CSV que contiene la cola.
     * @param modulo modulo responsable.
     */
    public RepositorioOperacionesPendientes(Path archivoPendientes, String modulo) {
        this.archivoPendientes = archivoPendientes;
        this.modulo = modulo;
    }

    /**
     * Devuelve todas las operaciones pendientes.
     *
     * @return lista ordenada por persistencia.
     * @throws ErrorPersistencia si no se puede leer el archivo.
     */
    public synchronized List<OperacionPendienteInventario> listar() throws ErrorPersistencia {
        asegurarArchivo();
        List<String[]> registros = ArchivoCsvUtil.leerRegistros(archivoPendientes, modulo);
        List<OperacionPendienteInventario> operaciones = new ArrayList<>();

        for (String[] columnas : registros) {
            operaciones.add(new OperacionPendienteInventario(
                    columnas[0],
                    columnas[1],
                    Integer.parseInt(columnas[2]),
                    columnas[3],
                    columnas[4],
                    columnas[5])
            );
        }

        return operaciones;
    }

    /**
     * Guarda una nueva operacion pendiente.
     *
     * @param operacion operacion a persistir.
     * @throws ErrorPersistencia si no se puede escribir.
     */
    public synchronized void guardar(OperacionPendienteInventario operacion) throws ErrorPersistencia {
        List<OperacionPendienteInventario> operaciones = listar();
        operaciones.add(operacion);
        guardarLista(operaciones);
    }

    /**
     * Elimina una operacion ya sincronizada.
     *
     * @param idOperacion identificador a eliminar.
     * @throws ErrorPersistencia si no se puede actualizar el archivo.
     */
    public synchronized void eliminar(String idOperacion) throws ErrorPersistencia {
        List<OperacionPendienteInventario> operaciones = listar();
        operaciones.removeIf(operacion -> operacion.idOperacion().equals(idOperacion));
        guardarLista(operaciones);
    }

    private void guardarLista(List<OperacionPendienteInventario> operaciones) throws ErrorPersistencia {
        List<String> lineas = new ArrayList<String>();
        lineas.add("idOperacion|clave|cantidad|responsable|marcaTiempo|origen");

        for (OperacionPendienteInventario operacion : operaciones) {
            lineas.add(operacion.idOperacion() + "|"
                    + operacion.clave() + "|"
                    + operacion.cantidad() + "|"
                    + operacion.responsable() + "|"
                    + operacion.marcaTiempo() + "|"
                    + operacion.origen()
            );
        }

        ArchivoCsvUtil.escribirArchivo(archivoPendientes, lineas, modulo);
    }

    private void asegurarArchivo() throws ErrorPersistencia {
        if (!archivoPendientes.toFile().exists()) {
            List<String> lineas = new ArrayList<>();
            lineas.add("idOperacion|clave|cantidad|responsable|marcaTiempo|origen");
            ArchivoCsvUtil.escribirArchivo(archivoPendientes, lineas, modulo);
        }
    }
}
