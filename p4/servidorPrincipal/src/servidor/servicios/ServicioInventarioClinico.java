package servidor.servicios;

import java.util.List;
import servidor.excepciones.CodigosError;
import servidor.excepciones.ErrorAplicacion;
import servidor.excepciones.ErrorReplicaNoDisponible;
import servidor.modelos.MedicamentoInventario;
import servidor.modelos.OperacionPendienteInventario;
import servidor.modelos.ResultadoReplicacionInventario;
import servidor.replicacion.ClienteReplicaXmlRpc;
import servidor.replicacion.SincronizadorInventarioPrincipal;
import servidor.repositorios.RepositorioInventarioClinico;
import servidor.repositorios.RepositorioOperacionesAplicadas;
import servidor.repositorios.RepositorioOperacionesPendientes;
import servidor.util.GeneradorIdOperacion;
import servidor.validacion.ValidadorEntrada;

/**
 * Servicio de inventario clinico con replicacion parcial.
 */
public final class ServicioInventarioClinico {

    private static final String MODULO = "INVENTARIO_CLINICO";

    private final RepositorioInventarioClinico repositorio;
    private final ClienteReplicaXmlRpc clienteReplica;
    private final RepositorioOperacionesPendientes repositorioPendientesHaciaReplica;
    private final RepositorioOperacionesAplicadas repositorioAplicadasDesdeReplica;
    private final SincronizadorInventarioPrincipal sincronizador;

    public ServicioInventarioClinico(RepositorioInventarioClinico repositorio,
            ClienteReplicaXmlRpc clienteReplica,
            RepositorioOperacionesPendientes repositorioPendientesHaciaReplica,
            RepositorioOperacionesAplicadas repositorioAplicadasDesdeReplica) {
        this.repositorio = repositorio;
        this.clienteReplica = clienteReplica;
        this.repositorioPendientesHaciaReplica = repositorioPendientesHaciaReplica;
        this.repositorioAplicadasDesdeReplica = repositorioAplicadasDesdeReplica;
        this.sincronizador = new SincronizadorInventarioPrincipal(
                clienteReplica,
                repositorio,
                repositorioPendientesHaciaReplica,
                repositorioAplicadasDesdeReplica
        );
    }

    public MedicamentoInventario consultarExistenciaMedicamento(String clave) throws ErrorAplicacion {
        sincronizador.sincronizarPendientes();
        ValidadorEntrada.validarTextoObligatorio(clave, "clave", MODULO);
        return repositorio.buscarPorClave(clave.trim());
    }

    public List<MedicamentoInventario> listarMedicamentosBajoMinimo() throws ErrorAplicacion {
        sincronizador.sincronizarPendientes();
        return repositorio.listarBajoMinimo();
    }

    /**
     * Registra una entrada local y luego intenta replicarla.
     *
     * <p>Orden de ejecucion:
     * <ol>
     * <li>Valida la entrada.</li>
     * <li>Escribe localmente.</li>
     * <li>Intenta replicar el cambio.</li>
     * <li>Devuelve estado total o parcial.</li>
     * </ol>
     *
     * @param clave clave del medicamento.
     * @param cantidad cantidad que entra al inventario.
     * @param responsable usuario que solicita la operacion.
     * @return resultado detallado para la demo.
     * @throws ErrorAplicacion si la validacion o la escritura local fallan.
     */
    public ResultadoReplicacionInventario registrarEntradaMedicamento(String clave, int cantidad, String responsable)
            throws ErrorAplicacion {

        sincronizador.sincronizarPendientes();
        ValidadorEntrada.validarTextoObligatorio(clave, "clave", MODULO);
        ValidadorEntrada.validarTextoObligatorio(responsable, "responsable", MODULO);
        ValidadorEntrada.validarEnteroPositivo(cantidad, "cantidad", MODULO);

        String idOperacion = GeneradorIdOperacion.generarId("PRINCIPAL");
        MedicamentoInventario actualizado = repositorio.registrarEntrada(clave.trim(), cantidad);

        try {
            clienteReplica.replicarEntradaMedicamento(idOperacion, clave.trim(), cantidad, responsable.trim());
            return new ResultadoReplicacionInventario(
                    CodigosError.OK,
                    "La entrada se registro en el servidor principal y en la replica.",
                    idOperacion,
                    actualizado,
                    true,
                    true,
                    "Replica sincronizada correctamente."
            );

        } catch (ErrorReplicaNoDisponible errorReplicaNoDisponible) {
            OperacionPendienteInventario pendiente = new OperacionPendienteInventario(
                    idOperacion,
                    clave.trim(),
                    cantidad,
                    responsable.trim(),
                    GeneradorIdOperacion.generarMarcaTiempo(),
                    "PRINCIPAL"
            );

            repositorioPendientesHaciaReplica.guardar(pendiente);

            return new ResultadoReplicacionInventario(
                    CodigosError.REPLICA_PENDIENTE,
                    "La entrada se registro en el servidor principal, pero la replica no pudo actualizarse.",
                    idOperacion,
                    actualizado,
                    true,
                    false,
                    errorReplicaNoDisponible.getMessage() + " | " + errorReplicaNoDisponible.getDetalleTecnico()
            );
        }
    }

    /**
     * Aplica localmente una operacion que se origino en la replica.
     *
     * <p>Este metodo no intenta replicar de regreso a la replica. Su unica
     * finalidad es absorber cambios pendientes que la replica ejecuto en modo
     * contingencia mientras el principal no estaba disponible.
     *
     * @param idOperacion identificador idempotente de la operacion.
     * @param clave clave del medicamento.
     * @param cantidad cantidad a sumar.
     * @param responsable actor que origino el cambio.
     * @return medicamento actualizado en el principal.
     * @throws ErrorAplicacion si los datos son invalidos o la persistencia falla.
     */
    public MedicamentoInventario registrarEntradaMedicamentoDesdeReplica(String idOperacion, String clave,
            int cantidad, String responsable) throws ErrorAplicacion {

        ValidadorEntrada.validarTextoObligatorio(idOperacion, "idOperacion", MODULO);
        ValidadorEntrada.validarTextoObligatorio(clave, "clave", MODULO);
        ValidadorEntrada.validarTextoObligatorio(responsable, "responsable", MODULO);
        ValidadorEntrada.validarEnteroPositivo(cantidad, "cantidad", MODULO);

        if (repositorioAplicadasDesdeReplica.yaAplicada(idOperacion)) {
            return repositorio.buscarPorClave(clave.trim());
        }

        MedicamentoInventario actualizado = repositorio.registrarEntrada(clave.trim(), cantidad);
        repositorioAplicadasDesdeReplica.marcarAplicada(new OperacionPendienteInventario(
                idOperacion,
                clave.trim(),
                cantidad,
                responsable.trim(),
                GeneradorIdOperacion.generarMarcaTiempo(),
                "REPLICA")
        );

        return actualizado;
    }
}
