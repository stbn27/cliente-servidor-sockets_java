package cliente;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;

/**
 * Cliente TCP para enviar solicitudes al servidor.
 * El cliente no procesa CURP ni matrícula UAM.
 * Solamente envía la cadena escrita por el usuario,
 * recibe la respuesta del servidor y la muestra en pantalla.
 *
 * @author esteban
 * @version 1.0
 */
public class Cliente {

    private static final String HOST = "localhost";
    private static final int PUERTO = 2000;

    /**
     * Método principal del cliente.
     *
     * @param args argumentos de la línea de comandos.
     */
    public static void main(String[] args) {

        try (Socket socket = new Socket(HOST, PUERTO);
             DataInputStream entrada = new DataInputStream(socket.getInputStream());
             DataOutputStream salida = new DataOutputStream(socket.getOutputStream());
             Scanner teclado = new Scanner(System.in)) {

            System.out.println("Conectado al servidor.");
            System.out.println();
            System.out.println("Escribe una solicitud con el formato:");
            System.out.println("C SABC560626MDFLRN09");
            System.out.println("M 2173075001");
            System.out.println("Escribe SALIR para terminar.");
            System.out.println();

            boolean continuar = true;

            while (continuar) {
                System.out.print("Ingrese solicitud: ");
                String mensaje = teclado.nextLine();

                salida.writeUTF(mensaje);

                String respuesta = entrada.readUTF();
                System.out.println("Respuesta del servidor: " + respuesta);

                if (mensaje.equalsIgnoreCase("SALIR")) {
                    continuar = false;
                }
            }

            System.out.println("Conexión finalizada.");

        } catch (IOException e) {
            System.out.println("Error en el cliente: " + e.getMessage());
        }
    }
}