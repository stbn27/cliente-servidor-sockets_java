package servidor.utilidades;

import servidor.error.CodigoError;
import servidor.sesion.SesionCliente;
import servidor.servicio.CineService;
import servidor.servicio.RespuestaServidor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Atiende la comunicacion de un cliente conectado y mantiene su sesion temporal.
 */
public class ClienteHandler implements Runnable {
    private static final AtomicInteger SECUENCIA_CLIENTES = new AtomicInteger(1);

    private final Socket socket;
    private final CineService service;
    private final SesionCliente sesion;

    /**
     * Crea el manejador de un cliente.
     *
     * @param socket socket del cliente.
     * @param service servicio compartido del servidor.
     */
    public ClienteHandler(Socket socket, CineService service) {
        this.socket = socket;
        this.service = service;
        this.sesion = new SesionCliente(String.format("C%03d", SECUENCIA_CLIENTES.getAndIncrement()));
    }

    /**
     * Ejecuta el ciclo de lectura y escritura del cliente.
     */
    @Override
    public void run() {
        boolean cierreSolicitado = false;
        System.out.println("Cliente conectado: " + sesion.getIdCliente() + " desde "
                + socket.getInetAddress().getHostAddress() + ".");
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
             PrintWriter writer = new PrintWriter(socket.getOutputStream(), true, StandardCharsets.UTF_8)) {

            enviarRespuestas(writer, service.crearPantallaInicial(sesion));

            String linea;
            while ((linea = reader.readLine()) != null) {
                System.out.println("Mensaje recibido de " + sesion.getIdCliente() + ": " + linea);
                List<String> respuestas = procesarSeguramente(linea);
                enviarRespuestas(writer, respuestas);
                if (contieneDesconexion(respuestas)) {
                    cierreSolicitado = true;
                    break;
                }
            }
        } catch (IOException excepcion) {
            System.out.println("Cliente desconectado por error de comunicacion: " + sesion.getIdCliente() + ".");
        } finally {
            if (!cierreSolicitado) {
                service.cerrarSesion(sesion);
            }
            cerrarSocket();
            System.out.println("Cliente desconectado: " + sesion.getIdCliente() + ".");
        }
    }

    /**
     * Procesa un mensaje capturando errores no controlados.
     *
     * @param linea linea recibida desde el cliente.
     * @return respuestas a enviar al cliente.
     */
    private List<String> procesarSeguramente(String linea) {
        try {
            return service.procesarMensaje(sesion, linea);
        } catch (RuntimeException excepcion) {
            System.out.println("Error interno al procesar la sesion " + sesion.getIdCliente() + ": "
                    + excepcion.getMessage());
            return List.of(RespuestaServidor.error(CodigoError.ERROR_INTERNO));
        }
    }

    /**
     * Envia todas las respuestas de una operacion al cliente.
     *
     * @param writer escritor del socket.
     * @param respuestas respuestas a enviar.
     */
    private void enviarRespuestas(PrintWriter writer, List<String> respuestas) {
        for (String respuesta : respuestas) {
            writer.println(respuesta);
            System.out.println("Respuesta enviada a " + sesion.getIdCliente() + ": " + respuesta);
        }
    }

    /**
     * Indica si el lote de respuestas contiene una orden de desconexion.
     *
     * @param respuestas respuestas emitidas por el servicio.
     * @return {@code true} si la conexion debe cerrarse.
     */
    private boolean contieneDesconexion(List<String> respuestas) {
        for (String respuesta : respuestas) {
            if (respuesta.startsWith("OK|DESCONEXION")) {
                return true;
            }
        }
        return false;
    }

    /**
     * Cierra el socket del cliente ignorando errores de cierre.
     */
    private void cerrarSocket() {
        try {
            socket.close();
        } catch (IOException ignored) {
            // El socket ya no puede aportar informacion adicional.
        }
    }
}
