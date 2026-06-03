package servidor;

import servidor.servicio.CineService;
import servidor.utilidades.ClienteHandler;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Clase principal que inicia el servidor de cine basado en sockets TCP.
 */
public class ServidorCine {
    private static final int PUERTO = 2000;

    /**
     * Inicia el servidor, muestra las funciones cargadas y acepta clientes concurrentes.
     *
     * @param args argumentos de linea de comandos.
     */
    public static void main(String[] args) {
        try {
            CineService service = new CineService();
            try (ServerSocket serverSocket = new ServerSocket(PUERTO)) {
                System.out.println("Servidor iniciado en el puerto " + PUERTO + ".");
                System.out.println("Funciones cargadas:");
                System.out.println();
                System.out.println(service.generarTablaFuncionesCargadas());
                System.out.println();
                System.out.println("Esperando clientes...");
                while (true) {
                    Socket socket = serverSocket.accept();
                    Thread hiloCliente = new Thread(new ClienteHandler(socket, service),
                            "cliente-" + socket.getPort());
                    hiloCliente.start();
                }
            }
        } catch (IOException excepcion) {
            System.out.println("Error al iniciar el servidor: " + excepcion.getMessage());
        }
    }
}
