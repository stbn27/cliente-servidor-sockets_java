package cliente.util;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

/**
 * Convierte respuestas RPC en salidas legibles para la consola.
 */
public final class FormateadorRespuestaRpc {

    private FormateadorRespuestaRpc() {
    }

    /**
     * Imprime una respuesta RPC con formato amigable.
     *
     * @param respuesta respuesta uniforme enviada por el servidor o creada por el cliente.
     */
    public static void imprimir(Hashtable<String, Object> respuesta) {

        boolean exito = Boolean.TRUE.equals(respuesta.get("exito"));
        String codigo = String.valueOf(respuesta.get("codigo"));
        String mensaje = String.valueOf(respuesta.get("mensaje"));
        String detalleTecnico = String.valueOf(respuesta.get("detalleTecnico"));
        String modulo = String.valueOf(respuesta.get("modulo"));
        Object origenAtencion = respuesta.get("origenAtencion");

        System.out.println();
        if (exito) {
            ConsolaDecoradora.exito(codigo + " | " + mensaje);
        } else {
            ConsolaDecoradora.error(codigo + " | " + mensaje);
        }

        System.out.println("\nModulo: " + modulo);

        if (origenAtencion != null && !"null".equals(String.valueOf(origenAtencion))) {
            System.out.println("Atendido por: SERVIDOR_" + origenAtencion);
        }

        if (detalleTecnico != null && !"null".equals(detalleTecnico) && !detalleTecnico.isEmpty()) {
            System.out.println("Detalle tecnico: " + detalleTecnico);
        }

        Object datos = respuesta.get("datos");
        if (datos != null) {
            System.out.println("Datos:");
            System.out.print(formatearObjeto(datos, "  "));
        }
    }

    private static String formatearObjeto(Object objeto, String sangria) {

        if (objeto instanceof Hashtable) {

            StringBuilder constructor = new StringBuilder();
            Hashtable<String, Object> tabla = (Hashtable<String, Object>) objeto;
            Enumeration<String> claves = tabla.keys();

            while (claves.hasMoreElements()) {
                Object clave = claves.nextElement();
                Object valor = tabla.get(clave);
                if (valor instanceof Hashtable || valor instanceof Vector) {
                    constructor.append(sangria).append("- ").append(clave).append(":\n");
                    constructor.append(formatearObjeto(valor, sangria + "  "));
                } else {
                    constructor.append(sangria).append("- ").append(clave).append(": ").append(valor).append('\n');
                }
            }

            return constructor.toString();
        }

        if (objeto instanceof Vector) {
            StringBuilder constructor = new StringBuilder();
            Vector<Object> vector = (Vector<Object>) objeto;

            if (vector.isEmpty()) {
                constructor.append(sangria).append("- sin registros\n");
                return constructor.toString();
            }

            for (int indice = 0; indice < vector.size(); indice++) {
                Object elemento = vector.elementAt(indice);
                if (elemento instanceof Hashtable || elemento instanceof Vector) {
                    constructor.append(sangria).append("- elemento ").append(indice + 1).append(":\n");
                    constructor.append(formatearObjeto(elemento, sangria + "  "));
                } else {
                    constructor.append(sangria).append("- ").append(elemento).append('\n');
                }
            }

            return constructor.toString();
        }

        return sangria + "- " + String.valueOf(objeto) + '\n';
    }
}
