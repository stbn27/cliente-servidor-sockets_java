package servidor.repositorios;

import java.nio.file.Path;
import java.util.List;
import servidor.excepciones.ErrorPersistencia;
import servidor.modelos.OperacionPendienteInventario;

/**
 * Registro de operaciones del principal ya absorbidas por la replica.
 */
public final class RepositorioOperacionesAplicadas {

    private final RepositorioOperacionesPendientes repositorioInterno;

    /**
     * Crea el historial de operaciones ya aplicadas.
     *
     * @param archivoAplicadas archivo CSV de historial.
     * @param modulo modulo responsable.
     */
    public RepositorioOperacionesAplicadas(Path archivoAplicadas, String modulo) {
        this.repositorioInterno = new RepositorioOperacionesPendientes(archivoAplicadas, modulo);
    }

    /**
     * Verifica si una operacion remota ya se absorbio localmente.
     *
     * @param idOperacion identificador remoto.
     * @return {@code true} si ya se aplico.
     * @throws ErrorPersistencia si no se puede leer el historial.
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
     * Marca una operacion remota como aplicada.
     *
     * @param operacion operacion remota absorbida por la replica.
     * @throws ErrorPersistencia si no se puede persistir.
     */
    public synchronized void marcarAplicada(OperacionPendienteInventario operacion) throws ErrorPersistencia {
        if (!yaAplicada(operacion.idOperacion())) {
            repositorioInterno.guardar(operacion);
        }
    }
}
