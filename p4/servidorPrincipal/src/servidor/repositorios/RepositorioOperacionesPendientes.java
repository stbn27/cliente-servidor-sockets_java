package servidor.repositorios;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import servidor.excepciones.ErrorPersistencia;
import servidor.modelos.OperacionPendienteInventario;
import servidor.persistencia.ArchivoCsvUtil;

/**
 * Persistencia sencilla para colas de sincronizacion pendientes.
 */
public final class RepositorioOperacionesPendientes {

    private final String modulo;
    private final Path archivoPendientes;

    /**
     * Crea el repositorio contra un CSV concreto.
     *
     * @param archivoPendientes ruta del archivo de pendientes.
     * @param modulo modulo responsable del archivo.
     */
    public RepositorioOperacionesPendientes(Path archivoPendientes, String modulo) {
        this.archivoPendientes = archivoPendientes;
        this.modulo = modulo;
    }

    /**
     * Lista todas las operaciones pendientes.
     *
     * @return operaciones en orden de persistencia.
     * @throws ErrorPersistencia si el archivo no puede leerse.
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
     * Agrega una operacion a la cola.
     *
     * @param operacion operacion pendiente de confirmar.
     * @throws ErrorPersistencia si no se puede escribir el archivo.
     */
    public synchronized void guardar(OperacionPendienteInventario operacion) throws ErrorPersistencia {
        List<OperacionPendienteInventario> operaciones = listar();
        operaciones.add(operacion);
        guardarLista(operaciones);
    }

    /**
     * Elimina una operacion por identificador.
     *
     * @param idOperacion identificador de la operacion.
     * @throws ErrorPersistencia si el archivo no puede actualizarse.
     */
    public synchronized void eliminar(String idOperacion) throws ErrorPersistencia {
        List<OperacionPendienteInventario> operaciones = listar();
        /*
        Iterator<OperacionPendienteInventario> iterador = operaciones.iterator();

        while (iterador.hasNext()) {
            OperacionPendienteInventario operacion = iterador.next();
            if (operacion.idOperacion().equals(idOperacion)) {
                iterador.remove();
            }
        }
        */
        operaciones.removeIf(operacion -> operacion.idOperacion().equals(idOperacion));
        guardarLista(operaciones);
    }

    private void guardarLista(List<OperacionPendienteInventario> operaciones) throws ErrorPersistencia {

        List<String> lineas = new ArrayList<>();
        lineas.add("idOperacion|clave|cantidad|responsable|marcaTiempo|origen");

        for (OperacionPendienteInventario operacion : operaciones) {
            lineas.add(operacion.idOperacion() + "|"
                    + operacion.clave() + "|"
                    + operacion.cantidad() + "|"
                    + operacion.responsable() + "|"
                    + operacion.marcaTiempo() + "|"
                    + operacion.origen());
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
