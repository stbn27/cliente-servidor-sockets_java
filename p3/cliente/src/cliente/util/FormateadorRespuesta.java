package cliente.util;

import cliente.estado.EstadoCliente;

import java.io.PrintStream;
import java.util.List;

/**
 * Presenta de forma amigable las respuestas del servidor y determina el siguiente estado del cliente.
 */
public class FormateadorRespuesta {
    /** Menu textual que actualmente inserta el servidor en la pantalla inicial. */
    private static final String MENU_SERVIDOR_INICIO = "1. Seleccionar funcion\n2. Cambiar fecha\n0. Salir";
    /** Menu textual que actualmente inserta el servidor en la pantalla de funcion. */
    private static final String MENU_SERVIDOR_FUNCION =
            "1. Seleccionar asiento\n2. Cambiar fecha\n3. Cambiar funcion\n0. Salir";
    /** Menu textual que actualmente inserta el servidor en la pantalla posterior al bloqueo. */
    private static final String MENU_SERVIDOR_BLOQUEO =
            "1. Confirmar compra\n2. Cambiar fecha\n3. Cambiar asiento\n4. Volver al inicio\n5. Salir";

    /** Longitud maxima de una linea de decorador para resaltar mensajes importantes. */
    private static final int LONGITUD_DECORADOR = 80;

    private final PrintStream salida;

    /**
     * Crea un formateador de respuestas para consola.
     *
     * @param salida flujo donde se imprimiran las respuestas legibles.
     */
    public FormateadorRespuesta(PrintStream salida) {
        this.salida = salida;
    }

    /**
     * Procesa un lote de respuestas y devuelve el estado final que debe usar el cliente.
     *
     * @param respuestas respuestas recibidas del servidor para una misma operacion.
     * @param estadoActual estado previo del cliente.
     * @return estado que debe quedar activo tras mostrar el lote completo.
     */
    public EstadoCliente procesarRespuestas(List<String> respuestas, EstadoCliente estadoActual) {
        EstadoCliente estadoResultado = estadoActual;
        for (String respuesta : respuestas) {
            estadoResultado = procesarRespuesta(respuesta, estadoResultado);
        }
        return estadoResultado;
    }

    /**
     * Procesa una respuesta individual del protocolo.
     *
     * @param respuesta respuesta individual enviada por el servidor.
     * @param estadoActual estado previo o acumulado del cliente.
     * @return estado actualizado despues de interpretar la respuesta.
     */
    private EstadoCliente procesarRespuesta(String respuesta, EstadoCliente estadoActual) {
        if (respuesta == null || respuesta.trim().isEmpty()) {
            salida.println("Sin respuesta del servidor.");
            return estadoActual;
        }

        String[] partes = ProtocoloCliente.dividirLinea(respuesta);
        if (partes.length == 0) {
            salida.println(ProtocoloCliente.hacerLegible(respuesta));
            return estadoActual;
        }

        if ("ERR".equals(partes[0])) {
            return imprimirError(partes, estadoActual);
        }

        if (!"OK".equals(partes[0])) {
            salida.println(ProtocoloCliente.hacerLegible(respuesta));
            return estadoActual;
        }

        return procesarRespuestaOk(partes, estadoActual, respuesta);
    }

    /**
     * Procesa una respuesta correcta del servidor.
     *
     * @param partes partes ya separadas del mensaje.
     * @param estadoActual estado previo o acumulado del cliente.
     * @param respuestaOriginal respuesta completa para casos no reconocidos.
     * @return estado actualizado tras interpretar la respuesta.
     */
    private EstadoCliente procesarRespuestaOk(String[] partes, EstadoCliente estadoActual, String respuestaOriginal) {
        String tipo = obtenerCampoLegible(partes, 1);
        switch (tipo) {
            case "PANTALLA":
                return imprimirPantalla(partes, estadoActual);
            case "ASIENTO_BLOQUEADO":
                imprimirAsientoBloqueado(partes);
                return EstadoCliente.MENU_ASIENTO_BLOQUEADO;
            case "BOLETO_GENERADO":
                imprimirBoleto(partes);
                return EstadoCliente.MENU_INICIO;
            case "DESCONEXION":
                imprimirMensajeSimple(partes);
                return EstadoCliente.SALIR;
            default:
                salida.println(ProtocoloCliente.hacerLegible(respuestaOriginal));
                return estadoActual;
        }
    }

    /**
     * Imprime una respuesta de error y decide si el flujo debe volver al inicio.
     *
     * @param partes partes ya separadas del mensaje.
     * @param estadoActual estado previo o acumulado del cliente.
     * @return estado que debe conservar el cliente tras el error.
     */
    private EstadoCliente imprimirError(String[] partes, EstadoCliente estadoActual) {
        String codigo = obtenerCampoLegible(partes, 1);
        String mensaje = obtenerCampoLegible(partes, 2);
        salida.println("Error: [" + codigo + "] " + mensaje);
        if ("ERR_TIEMPO_BLOQUEO_EXPIRADO".equals(codigo) || "ERR_SESION_INVALIDA".equals(codigo)) {
            return EstadoCliente.MENU_INICIO;
        }
        return estadoActual;
    }

    /**
     * Imprime una pantalla enviada por el servidor y actualiza el estado activo.
     *
     * @param partes partes ya separadas del mensaje.
     * @param estadoActual estado previo o acumulado del cliente.
     * @return estado indicado por la pantalla recibida.
     */
    private EstadoCliente imprimirPantalla(String[] partes, EstadoCliente estadoActual) {
        String contenido = obtenerCampoLegible(partes, 2);
        String identificadorMenu = obtenerCampoLegible(partes, 3);
        EstadoCliente nuevoEstado = EstadoCliente.desdeIdentificador(identificadorMenu);
        if (nuevoEstado == EstadoCliente.SALIR) {
            nuevoEstado = estadoActual;
        }
        salida.println(limpiarContenidoPantalla(contenido, nuevoEstado));
        return nuevoEstado;
    }

    /**
     * Imprime el resultado del bloqueo temporal de un asiento.
     *
     * @param partes partes ya separadas del mensaje.
     */
    private void imprimirAsientoBloqueado(String[] partes) {
        String funcion = obtenerCampoLegible(partes, 2);
        String asiento = obtenerCampoLegible(partes, 3);
        String expiracion = obtenerCampoLegible(partes, 4);
        String mensaje = obtenerCampoLegible(partes, 5);

        salida.println("Asiento " + asiento + " bloqueado correctamente.");
        if (!funcion.isEmpty()) {
            salida.println("Funcion: " + funcion);
        }
        if (!expiracion.isEmpty()) {
            salida.println("Expira: " + expiracion);
        }
        if (!mensaje.isEmpty()) {
            salida.println(mensaje);
        }
    }

    /**
     * Imprime un boleto generado de forma legible para consola.
     *
     * @param partes partes ya separadas del mensaje.
     */
    private void imprimirBoleto(String[] partes) {
        salida.println("Boleto generado correctamente");
        salida.println("ID boleto: " + obtenerCampoLegible(partes, 2));
        salida.println("Mensaje: " + obtenerCampoLegible(partes, 3));
        for (int indice = 4; indice < partes.length; indice++) {
            salida.println(obtenerCampoLegible(partes, indice));
        }
    }

    /**
     * Imprime un mensaje simple de respuesta correcta.
     *
     * @param partes partes ya separadas del mensaje.
     */
    private void imprimirMensajeSimple(String[] partes) {
        String mensaje = obtenerCampoLegible(partes, 2);
        if (!mensaje.isEmpty()) {
            salida.println(mensaje);
        }
    }

    /**
     * Obtiene un campo legible de la respuesta si el indice existe.
     *
     * @param partes partes ya separadas del mensaje.
     * @param indice posicion del campo deseado.
     * @return valor legible o cadena vacia si no existe.
     */
    private String obtenerCampoLegible(String[] partes, int indice) {
        if (partes == null || indice < 0 || indice >= partes.length) {
            return "";
        }
        return ProtocoloCliente.hacerLegible(partes[indice]);
    }

    /**
     * Elimina el menu textual incrustado por el servidor para evitar duplicarlo en consola.
     *
     * @param contenido contenido completo enviado por el servidor.
     * @param estadoActual estado asociado a la pantalla recibida.
     * @return contenido listo para imprimirse sin el menu textual del servidor.
     */
    private String limpiarContenidoPantalla(String contenido, EstadoCliente estadoActual) {
        String normalizado = contenido.replace("\r\n", "\n").replace('\r', '\n');
        String menuServidor = obtenerMenuServidor(estadoActual);
        if (!menuServidor.isEmpty() && normalizado.endsWith(menuServidor)) {
            normalizado = normalizado.substring(0, normalizado.length() - menuServidor.length());
            normalizado = eliminarSaltosFinales(normalizado);
        }
        return normalizado.replace("\n", System.lineSeparator());
    }

    /**
     * Obtiene la variante textual del menu que hoy agrega el servidor al contenido.
     *
     * @param estadoActual estado asociado a la pantalla recibida.
     * @return menu textual del servidor o cadena vacia si no aplica.
     */
    private String obtenerMenuServidor(EstadoCliente estadoActual) {
        switch (estadoActual) {
            case MENU_INICIO:
                return MENU_SERVIDOR_INICIO;
            case MENU_FUNCION:
                return MENU_SERVIDOR_FUNCION;
            case MENU_ASIENTO_BLOQUEADO:
                return MENU_SERVIDOR_BLOQUEO;
            case SALIR:
                return "";
            default:
                return "";
        }
    }

    /**
     * Elimina saltos de linea sobrantes al final de un bloque de texto.
     *
     * @param texto texto potencialmente terminado en saltos de linea.
     * @return texto sin saltos finales redundantes.
     */
    private String eliminarSaltosFinales(String texto) {
        int fin = texto.length();
        while (fin > 0) {
            char caracter = texto.charAt(fin - 1);
            if (caracter != '\n' && caracter != '\r') {
                break;
            }
            fin--;
        }
        return texto.substring(0, fin);
    }

    /**
     * Repite un caracter tantas veces como lo indique la constante LONGITUD_DECORADOR.
     * @param c caracter a repetir para formar el decorador.
     * @param saltoInicio Salta de linea al principio del decorador.
     * @param saltoFin Salta de linea al final del decorador.
     * @return decorador con el caracter repetido.
     */
    public void marcoTexto(String c, boolean saltoInicio, boolean saltoFin) {

        // Verifica que no sea null o vacio, caso contrario se usa "=" por defecto
        if (c == null || c.isEmpty()) {
            c = "=";
        }

        StringBuilder builder = new StringBuilder();
        if (saltoInicio) {
            builder.append(System.lineSeparator());
        }
        builder.repeat(c, LONGITUD_DECORADOR);
        if (saltoFin) {
            builder.append(System.lineSeparator());
        }
        this.salida.println(builder);
    }
}
