package cliente.servicio;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Gestiona la comunicacion TCP del cliente con el servidor de cine.
 */
public class ClienteSocketService {
    /** Tiempo maximo para establecer la conexion inicial. */
    private static final int TIMEOUT_CONEXION_MS = 5000;
    /** Tiempo maximo para esperar la primera respuesta de un lote. */
    private static final int TIMEOUT_LECTURA_MS = 5000;
    /** Tiempo corto usado para drenar respuestas consecutivas del mismo lote. */
    private static final int TIMEOUT_LOTE_MS = 200;

    private final String host;
    private final int puerto;
    private final PrintStream salida;
    private Socket socket;
    private BufferedReader reader;
    private PrintWriter writer;

    /**
     * Crea el servicio de socket con la direccion del servidor.
     *
     * @param host direccion IP o nombre del servidor.
     * @param puerto puerto TCP del servidor.
     * @param salida flujo de salida usado para reportar errores de cierre.
     */
    public ClienteSocketService(String host, int puerto, PrintStream salida) {
        this.host = host;
        this.puerto = puerto;
        this.salida = salida;
    }

    /**
     * Abre la conexion TCP y prepara los flujos de lectura y escritura.
     *
     * @throws IOException si ocurre un error al conectarse.
     */
    public void conectar() throws IOException {
        socket = new Socket();
        socket.connect(new InetSocketAddress(host, puerto), TIMEOUT_CONEXION_MS);
        socket.setSoTimeout(TIMEOUT_LECTURA_MS);
        reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
        writer = new PrintWriter(socket.getOutputStream(), true, StandardCharsets.UTF_8);
    }

    /**
     * Envia un mensaje textual al servidor.
     *
     * @param mensaje mensaje que sera enviado al servidor.
     * @throws IOException si no existe una conexion activa o falla el envio.
     */
    public void enviarMensaje(String mensaje) throws IOException {
        validarConexion();
        writer.println(mensaje);
        if (writer.checkError()) {
            throw new IOException("No fue posible enviar la solicitud al servidor.");
        }
    }

    /**
     * Recibe un lote de respuestas consecutivas emitidas por el servidor.
     *
     * @return lista de lineas recibidas para una misma operacion.
     * @throws IOException si ocurre un error durante la lectura.
     */
    public List<String> recibirRespuestas() throws IOException {
        validarConexion();
        List<String> respuestas = new ArrayList<>();
        int timeoutOriginal = socket.getSoTimeout();
        try {
            respuestas.add(leerLineaObligatoria());
            drenarRespuestasPendientes(respuestas);
            return respuestas;
        } finally {
            restaurarTimeout(timeoutOriginal);
        }
    }

    /**
     * Cierra ordenadamente los recursos asociados a la conexion.
     */
    public void cerrar() {
        if (writer != null) {
            writer.close();
        }
        try {
            if (reader != null) {
                reader.close();
            }
        } catch (IOException excepcion) {
            salida.println("Error al cerrar lectura: " + excepcion.getMessage());
        }
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException excepcion) {
            salida.println("Error al cerrar socket: " + excepcion.getMessage());
        }
    }

    /**
     * Lee la primera linea obligatoria de un lote de respuestas.
     *
     * @return linea recibida desde el servidor.
     * @throws IOException si la conexion fue cerrada o la lectura falla.
     */
    private String leerLineaObligatoria() throws IOException {
        String respuesta = reader.readLine();
        if (respuesta == null) {
            throw new IOException("Conexion cerrada por el servidor.");
        }
        return respuesta;
    }

    /**
     * Intenta leer respuestas adicionales del mismo lote sin bloquear demasiado tiempo.
     *
     * @param respuestas lista donde se acumulan las lineas recibidas.
     * @throws IOException si ocurre un error inesperado al leer.
     */
    private void drenarRespuestasPendientes(List<String> respuestas) throws IOException {
        socket.setSoTimeout(TIMEOUT_LOTE_MS);
        try {
            while (true) {
                String respuesta = reader.readLine();
                if (respuesta == null) {
                    return;
                }
                respuestas.add(respuesta);
            }
        } catch (SocketTimeoutException ignored) {
            // El lote termino cuando no llegaron mas lineas dentro del tiempo corto.
        }
    }

    /**
     * Restaura el timeout original del socket despues de drenar un lote.
     *
     * @param timeoutOriginal timeout previo del socket.
     */
    private void restaurarTimeout(int timeoutOriginal) {
        if (socket == null || socket.isClosed()) {
            return;
        }
        try {
            socket.setSoTimeout(timeoutOriginal);
        } catch (IOException excepcion) {
            salida.println("Error al restaurar timeout del socket: " + excepcion.getMessage());
        }
    }

    /**
     * Verifica que la conexion y sus flujos esten disponibles.
     *
     * @throws IOException si la conexion no esta activa.
     */
    private void validarConexion() throws IOException {
        if (socket == null || socket.isClosed() || reader == null || writer == null) {
            throw new IOException("No hay conexion activa con el servidor.");
        }
    }
}
