package servidor.servicio;

import servidor.error.CodigoError;
import servidor.utilidades.Protocolo;

/**
 * Construye mensajes de respuesta para el protocolo textual del servidor.
 */
public final class RespuestaServidor {

    /**
     * Constructor privado para evitar instanciacion.
     */
    private RespuestaServidor() {
        // Clase de utilidad.
    }

    /**
     * Construye una respuesta satisfactoria.
     *
     * @param partes partes de la respuesta a enviar.
     * @return linea completa del protocolo.
     */
    public static String ok(String... partes) {
        String[] todasLasPartes = new String[partes.length + 1];
        todasLasPartes[0] = "OK";
        System.arraycopy(partes, 0, todasLasPartes, 1, partes.length);
        return construir(todasLasPartes);
    }

    /**
     * Construye una respuesta de error usando el mensaje base del enum.
     *
     * @param codigoError codigo de error.
     * @return linea completa del protocolo.
     */
    public static String error(CodigoError codigoError) {
        return construir("ERR", codigoError.getCodigo(), codigoError.getMensaje());
    }

    /**
     * Construye una respuesta de error con un mensaje personalizado.
     *
     * @param codigoError codigo de error.
     * @param mensaje mensaje concreto a enviar.
     * @return linea completa del protocolo.
     */
    public static String error(CodigoError codigoError, String mensaje) {
        return construir("ERR", codigoError.getCodigo(), mensaje);
    }

    /**
     * Construye una linea del protocolo escapando cada campo.
     *
     * @param partes partes que integran el mensaje.
     * @return linea construida.
     */
    private static String construir(String... partes) {
        StringBuilder constructor = new StringBuilder();
        for (int indice = 0; indice < partes.length; indice++) {
            if (indice > 0) {
                constructor.append('|');
            }
            constructor.append(Protocolo.escaparCampo(partes[indice]));
        }
        return constructor.toString();
    }
}
