package cliente;

import cliente.estado.EstadoCliente;
import cliente.servicio.ClienteSocketService;
import cliente.ui.MenuCliente;
import cliente.util.FormateadorRespuesta;

import java.io.IOException;
import java.io.PrintStream;
import java.util.List;
import java.util.Scanner;

/**
 * Controla el flujo general del cliente de cine por consola.
 */
public class ClienteCine {
    /** IP por defecto usada cuando el usuario no captura un valor. */
    private static final String DEFAULT_HOST = "127.0.0.1";

    /** Puerto por defecto usado cuando el usuario no captura un valor. */
    private static final int DEFAULT_PUERTO = 2000;

    private final MenuCliente menu;
    private final ClienteSocketService socketService;
    private final FormateadorRespuesta formateador;
    private final PrintStream salida;
    private boolean conexionEstablecida;

    /**
     * Crea un cliente de cine con sus colaboradores principales.
     *
     * @param menu componente encargado de la interaccion por consola.
     * @param socketService servicio encargado de la comunicacion TCP.
     * @param formateador componente que muestra respuestas legibles.
     * @param salida flujo de salida del cliente.
     */
    public ClienteCine(
            MenuCliente menu,
            ClienteSocketService socketService,
            FormateadorRespuesta formateador,
            PrintStream salida
    ) {
        this.menu = menu;
        this.socketService = socketService;
        this.formateador = formateador;
        this.salida = salida;
        this.conexionEstablecida = false;
    }

    /**
     * Punto de entrada del cliente de cine.
     *
     * @param args argumentos de linea de comandos.
     */
    public static void main(String[] args) {

        // Salida por defecto para imprimir mensajes en consola. Se puede redirigir a un archivo o flujo.
        PrintStream salida = System.out;

        try (Scanner scanner = new Scanner(System.in)) {
            MenuCliente menu = new MenuCliente(scanner, salida);

            /*
            // Solicitar datos del servidor desde el menu.
            String host = menu.solicitarHost(DEFAULT_HOST);
            int puerto = menu.solicitarPuerto(DEFAULT_PUERTO);

            // Datos solicitados
            ClienteSocketService socketService = new ClienteSocketService(host, puerto, salida);
            */

            // Datos por defecto (HOST, PUERTO)
            ClienteSocketService socketService = new ClienteSocketService(DEFAULT_HOST, DEFAULT_PUERTO, salida);

            // Formateador de respuestas que se encargara de interpretar y mostrar las respuestas del servidor de forma legible.
            FormateadorRespuesta formateador = new FormateadorRespuesta(salida);

            // Crea el cliente con sus colaboradores principales.
            ClienteCine cliente = new ClienteCine(menu, socketService, formateador, salida);

            // Ejecuta el flujo completo del cliente hasta salir o detectar un error de comunicacion.
            cliente.ejecutar();
        }
    }

    /**
     * Ejecuta el flujo completo del cliente hasta salir o detectar un error de comunicacion.
     */
    public void ejecutar() {

        // Mensaje inicial en la terminal del cliente
        formateador.marcoTexto("-", true, false);
        salida.println("Conectando al servidor...");
        formateador.marcoTexto("-", false, false);

        // Conexion al servidor y ciclo principal de solicitud-respuesta
        try {

            // Conexión al servidor
            socketService.conectar();
            conexionEstablecida = true;

            salida.println("Conectado correctamente.");
            formateador.marcoTexto("=", false, true);

            // Inicialización
            ejecutarFlujoPrincipal();

        } catch (IOException excepcion) {
            formateador.marcoTexto("*", true, false);
            salida.println("Error: " + excepcion.getMessage());
            formateador.marcoTexto("*", false, true);

        } finally {
            cerrarConexion();
        }
    }

    /**
     * Ejecuta el ciclo principal de solicitud-respuesta del cliente.
     *
     * @throws IOException si ocurre un error durante la comunicacion con el servidor.
     */
    private void ejecutarFlujoPrincipal() throws IOException {

        // Recibe el mensaje del servidor
        // Posteriormente muestra el menú de opciones(Primer estado)
        EstadoCliente estadoActual = procesarRespuestas(socketService.recibirRespuestas(), EstadoCliente.MENU_INICIO);

        // Procesa las peticones del cliente, mientras no seleccione salir
        while (estadoActual != EstadoCliente.SALIR) {

            // Menú de opciones según el estado actual del cliente
            menu.mostrarMenu(estadoActual);

            // Solicita el texto respectivo(Id funcion, Id asiento,...) según el estado actual
            String solicitud = menu.construirSolicitud(estadoActual);

            // Envía la solicitud al servidor
            socketService.enviarMensaje(solicitud);

            // Muestra la solicitud enviada al servidor
            salida.println("Solicitud enviada: " + solicitud);
            formateador.marcoTexto("-", false, true);

            // Cambia el estado actual
            estadoActual = procesarRespuestas(socketService.recibirRespuestas(), estadoActual);
        }
    }

    /**
     * Registra y muestra un lote de respuestas del servidor.
     *
     * @param respuestas respuestas recibidas tras una solicitud.
     * @param estadoBase estado previo del cliente antes de procesar el lote.
     * @return siguiente estado que debe usar el cliente.
     */
    private EstadoCliente procesarRespuestas(List<String> respuestas, EstadoCliente estadoBase) {

        /* Imprimir en la consola el texto tal cual lo envia el servidor */
        //imprimirRespuestasRecibidas(respuestas);

        /* Imprime el texto en formateado para que sea mas fácil de leer */
        return formateador.procesarRespuestas(respuestas, estadoBase);
    }

    /**
     * @deprecated Metodo utilizado para debuguear las respuestas del servidor<br>
     *
     * Imprime en consola las respuestas crudas recibidas desde el servidor.
     *
     * @param respuestas respuestas emitidas por el servidor.
     */
    private void imprimirRespuestasRecibidas(List<String> respuestas) {
        for (String respuesta : respuestas) {
            salida.println("Respuesta recibida: " + respuesta);
        }
    }

    /**
     * Cierra la conexion si esta abierta e informa el resultado al usuario.
     */
    private void cerrarConexion() {
        if (!conexionEstablecida) {
            return;
        }

        formateador.marcoTexto("-", true, false);
        salida.println("Cerrando conexion...");
        socketService.cerrar();
        salida.println("Conexion finalizada.");
        formateador.marcoTexto("-", false, true);
    }
}
