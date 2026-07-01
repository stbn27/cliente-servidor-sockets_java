package cliente.menu;

import java.util.Hashtable;
import java.util.Vector;
import cliente.rpc.ClienteRpc;
import cliente.util.ConsolaDecoradora;
import cliente.util.FormateadorRespuestaRpc;
import cliente.util.LectorConsola;

/**
 * Controla el flujo del menu persistente del cliente.
 */
public final class AplicacionClienteConsola {

    private final ClienteRpc clienteRpc;
    private final LectorConsola lectorConsola;

    /**
     * Constructor de la clase.
     * @param clienteRpc cliente RPC para invocar servicios.
     */
    public AplicacionClienteConsola(ClienteRpc clienteRpc) {
        this.clienteRpc = clienteRpc;
        this.lectorConsola = new LectorConsola();
    }

    /**
     * Inicia el menu principal y solo termina cuando el usuario elige salir.
     */
    public void iniciar() {
        boolean continuar = true;
        while (continuar) {
            ConsolaDecoradora.titulo("Menu principal");
            System.out.println("  1) Redes sociales");
            System.out.println("  2) Movilidad urbana");
            System.out.println("  3) Inventario clinico");
            System.out.println("  4) Salir");

            int opcion = lectorConsola.leerEnteroEnRango("Selecciona una categoria", 1, 4);

            switch (opcion) {
                case 1:
                    menuRedesSociales();
                    break;
                case 2:
                    menuMovilidadUrbana();
                    break;
                case 3:
                    menuInventarioClinico();
                    break;
                case 4:
                    continuar = false;
                    break;
                default:
                    ConsolaDecoradora.error("La opcion seleccionada no es valida.");
                    pausa();
            }
        }
        ConsolaDecoradora.exito("Sesion finalizada por el usuario.");
    }

    private void menuRedesSociales() {
        ConsolaDecoradora.subtitulo("Redes sociales");
        System.out.println("  1) Consultar tendencias por pais");
        System.out.println("  2) Consultar estadisticas de usuario");
        System.out.println("  3) Publicar una nueva publicacion");
        System.out.println("  4) Volver");

        int opcion = lectorConsola.leerEnteroEnRango("Selecciona una operacion", 1, 4);

        switch (opcion) {
            case 1:
                String pais = lectorConsola.leerTextoNoVacio("Pais");
                mostrarRespuesta(clienteRpc.invocar(
                            "RedesSociales.consultarTendenciasPorPais",
                            vectorCon(pais)
                        )
                );
                break;
            case 2:
                String usuario = lectorConsola.leerTextoNoVacio("Usuario");
                mostrarRespuesta(clienteRpc.invocar(
                            "RedesSociales.consultarEstadisticasUsuario",
                            vectorCon(usuario)
                        )
                );
                break;
            case 3:
                String usuarioPublicacion = lectorConsola.leerTextoNoVacio("Usuario");
                String paisPublicacion = lectorConsola.leerTextoNoVacio("Pais");
                String mensaje = lectorConsola.leerTextoNoVacio("Mensaje");
                mostrarRespuesta(clienteRpc.invocar(
                            "RedesSociales.publicarNuevaPublicacion",
                            vectorCon(usuarioPublicacion, paisPublicacion, mensaje)
                        )
                );
                break;
            case 4:
                // Salir del menu
                return;
            default:
                ConsolaDecoradora.error("La opcion seleccionada no es valida.");
        }
        pausa();
    }

    private void menuMovilidadUrbana() {
        ConsolaDecoradora.subtitulo("Movilidad urbana");
        System.out.println("  1) Consultar trafico por zona");
        System.out.println("  2) Consultar eco-bicicletas por estacion");
        System.out.println("  3) Reportar incidente vial");
        System.out.println("  4) Volver");

        int opcion = lectorConsola.leerEnteroEnRango("Selecciona una operacion", 1, 4);

        switch (opcion) {
            case 1:
                String zona = lectorConsola.leerTextoNoVacio("Zona");
                mostrarRespuesta(clienteRpc.invocar(
                            "MovilidadUrbana.consultarTraficoPorZona",
                            vectorCon(zona)
                        )
                );
                break;
            case 2:
                String estacion = lectorConsola.leerTextoNoVacio("Estacion");
                mostrarRespuesta(clienteRpc.invocar(
                            "MovilidadUrbana.consultarEcoBicicletasPorEstacion",
                            vectorCon(estacion)
                        )
                );
                break;
            case 3:
                String zonaIncidente = lectorConsola.leerTextoNoVacio("Zona");
                String tipo = lectorConsola.leerTextoNoVacio("Tipo de incidente");
                String descripcion = lectorConsola.leerTextoNoVacio("Descripcion");
                String severidad = lectorConsola.leerTextoNoVacio("Severidad");
                String reportadoPor = lectorConsola.leerTextoNoVacio("Reportado por");
                mostrarRespuesta(clienteRpc.invocar(
                        "MovilidadUrbana.reportarIncidenteVial",
                            vectorCon(zonaIncidente, tipo, descripcion, severidad, reportadoPor)
                        )
                );
                break;
            case 4:
                // Salir del menú [Volver]
                return;
            default:
                ConsolaDecoradora.error("La opcion seleccionada no es valida.");
        }
        pausa();
    }

    private void menuInventarioClinico() {
        ConsolaDecoradora.subtitulo("Inventario clinico");
        System.out.println("  1) Consultar existencia por clave");
        System.out.println("  2) Listar medicamentos bajo minimo");
        System.out.println("  3) Registrar entrada de medicamento");
        System.out.println("  4) Volver");

        int opcion = lectorConsola.leerEnteroEnRango("Selecciona una operacion", 1, 4);

        switch (opcion) {
            case 1:
                String clave = lectorConsola.leerTextoNoVacio("Clave del medicamento");

                mostrarRespuesta(clienteRpc.invocar(
                            "InventarioClinico.consultarExistenciaMedicamento",
                            vectorCon(clave)
                        )
                );
                break;
            case 2:
                mostrarRespuesta(clienteRpc.invocar(
                            "InventarioClinico.listarMedicamentosBajoMinimo",
                            new Vector<>()
                        )
                );
                break;
            case 3:
                String claveEntrada = lectorConsola.leerTextoNoVacio("Clave del medicamento");
                int cantidad = lectorConsola.leerEnteroPositivo("Cantidad");
                String responsable = lectorConsola.leerTextoNoVacio("Responsable");

                mostrarRespuesta(clienteRpc.invocar(
                            "InventarioClinico.registrarEntradaMedicamento",
                            vectorCon(claveEntrada, cantidad, responsable)
                        )
                );
                break;
            case 4:
                // Salir del menú [Volver]
                return;
            default:
                ConsolaDecoradora.error("La opcion seleccionada no es valida.");
        }
        pausa();
    }

    private void mostrarRespuesta(Hashtable<String, Object> respuesta) {
        FormateadorRespuestaRpc.imprimir(respuesta);
    }

    private void pausa() {
        lectorConsola.esperarEnter("Presiona ENTER para volver al menu principal");
    }

    private Vector<Object> vectorCon(Object... parametros) {
        Vector<Object> vector = new Vector<>();

        for (Object parametro : parametros) {
            vector.addElement(parametro);
        }
        return vector;
    }
}
