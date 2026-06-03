package servidor;

import servidor.sesion.SesionCliente;
import servidor.servicio.CineService;
import servidor.utilidades.Protocolo;

import java.io.IOException;
import java.util.List;

/**
 * Ejecuta una verificacion local del flujo del servidor sin necesidad de sockets.
 * Carga los datos de los archivos txt que simulan los datos de <b>base de datos</b>.
 */
public class PruebaLocal {

    /**
     * Ejecuta una secuencia corta de prueba sobre el flujo guiado.
     *
     * @param args argumentos de linea de comandos.
     */
    public static void main(String[] args) {
        try {
            CineService service = new CineService();
            SesionCliente sesion = new SesionCliente("LOCAL-001");

            // Imprime
            imprimirRespuestas(service.crearPantallaInicial(sesion));
            imprimirRespuestas(service.procesarMensaje(sesion, "SELECCIONAR_FUNCION|F001"));
            imprimirRespuestas(service.procesarMensaje(sesion, "SELECCIONAR_ASIENTO|A1|JUAN"));
            imprimirRespuestas(service.procesarMensaje(sesion, "VOLVER_INICIO"));
        } catch (IOException excepcion) {
            System.out.println("Fallo la prueba local: " + excepcion.getMessage());
        }
    }

    /**
     * Imprime las respuestas del servicio en una version legible para consola.
     *
     * @param respuestas respuestas emitidas por el servicio.
     */
    private static void imprimirRespuestas(List<String> respuestas) {
        for (String respuesta : respuestas) {
            System.out.println("----------------------------------------");
            imprimirRespuesta(respuesta);
        }
    }

    /**
     * Imprime una respuesta del protocolo separando el contenido legible.
     *
     * @param respuesta respuesta individual del servidor.
     */
    private static void imprimirRespuesta(String respuesta) {
        String[] partes = Protocolo.dividirLinea(respuesta);
        if (partes.length >= 4 && "OK".equals(partes[0]) && "PANTALLA".equals(partes[1])) {
            System.out.println(partes[0] + "|" + partes[1] + "|" + partes[3]);
            System.out.println(Protocolo.hacerLegible(partes[2]));
            return;
        }
        System.out.println(Protocolo.hacerLegible(respuesta));
    }
}
