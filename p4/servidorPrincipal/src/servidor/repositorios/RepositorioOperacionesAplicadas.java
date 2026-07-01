package servidor.repositorios;

import java.nio.file.Path;
import java.util.List;
import servidor.excepciones.ErrorPersistencia;
import servidor.modelos.OperacionPendienteInventario;

/**
 * Registro de operaciones remotas ya aplicadas para asegurar idempotencia.
 */
public final class RepositorioOperacionesAplicadas {

    private final RepositorioOperacionesPendientes repositorioInterno;

    /**
     * Crea el registro de operaciones ya aplicadas.
     *
     * @param archivoAplicadas archivo CSV donde se conservara el historial.
     * @param modulo modulo responsable.
     */
    public RepositorioOperacionesAplicadas(Path archivoAplicadas, String modulo) {
        this.repositorioInterno = new RepositorioOperacionesPendientes(archivoAplicadas, modulo);
    }

    /**
     * Indica si una operacion remota ya se aplico localmente.
     *
     * @param idOperacion identificador remoto.
     * @return {@code true} si la operacion ya fue registrada.
     * @throws ErrorPersistencia si no se puede consultar el archivo.
     */
    public synchronized boolean yaAplicada(String idOperacion) throws ErrorPersistencia {

        List<OperacionPendienteInventario> operaciones = repositorioInterno.listar();

        for (OperacionPendienteInventario operacion : operaciones) {
            if (operacion.idOperacion().equals(idOperacion)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Guarda la operacion como aplicada.
     *
     * @param operacion operacion remota aceptada localmente.
     * @throws ErrorPersistencia si no se puede actualizar el archivo.
     */
    public synchronized void marcarAplicada(OperacionPendienteInventario operacion) throws ErrorPersistencia {

        if (!yaAplicada(operacion.idOperacion())) {
            repositorioInterno.guardar(operacion);
        }

    }
}
