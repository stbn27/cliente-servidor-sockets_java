package cliente.ui;

import cliente.estado.EstadoCliente;

import java.io.PrintStream;
import java.util.Scanner;

/**
 * Gestiona los menus de consola y la captura basica de datos del usuario.
 */
public class MenuCliente {
    private final Scanner scanner;
    private final PrintStream salida;
    private String ultimoNombreCliente;

    /**
     * Crea un menu de consola con sus dependencias basicas.
     *
     * @param scanner lector de entrada estandar.
     * @param salida flujo donde se muestran las instrucciones.
     */
    public MenuCliente(Scanner scanner, PrintStream salida) {
        this.scanner = scanner;
        this.salida = salida;
        this.ultimoNombreCliente = "";
    }

    /**
     * Solicita la IP del servidor permitiendo aceptar un valor por defecto.
     *
     * @param hostPorDefecto host usado cuando el usuario envia una cadena vacia.
     * @return host capturado o el valor por defecto.
     */
    public String solicitarHost(String hostPorDefecto) {
        salida.print("Ingrese la IP del servidor: ");
        String entrada = scanner.nextLine().trim();
        if (entrada.isEmpty()) {
            return hostPorDefecto;
        }
        return entrada;
    }

    /**
     * Solicita el puerto del servidor permitiendo aceptar un valor por defecto.
     *
     * @param puertoPorDefecto puerto usado cuando el usuario envia una cadena vacia.
     * @return puerto capturado o el valor por defecto.
     */
    public int solicitarPuerto(int puertoPorDefecto) {
        while (true) {
            salida.print("Ingrese el puerto del servidor: ");
            String entrada = scanner.nextLine().trim();
            if (entrada.isEmpty()) {
                return puertoPorDefecto;
            }
            try {
                int puerto = Integer.parseInt(entrada);
                if (puerto > 0 && puerto <= 65535) {
                    return puerto;
                }
            } catch (NumberFormatException ignored) {
                // Se vuelve a pedir el valor.
            }
            salida.println("Error: El puerto debe ser un numero entre 1 y 65535.");
        }
    }

    /**
     * Muestra el menu correspondiente al estado actual del cliente.
     *
     * @param estadoActual estado desde el cual se debe capturar la siguiente accion.
     */
    public void mostrarMenu(EstadoCliente estadoActual) {
        salida.println();
        switch (estadoActual) {
            case MENU_INICIO:
                salida.println("1. Seleccionar funcion");
                salida.println("2. Cambiar fecha");
                salida.println("3. Salir");
                break;
            case MENU_FUNCION:
                salida.println("1. Seleccionar asiento");
                salida.println("2. Cambiar fecha");
                salida.println("3. Cambiar funcion");
                salida.println("4. Salir");
                break;
            case MENU_ASIENTO_BLOQUEADO:
                salida.println("1. Confirmar compra");
                salida.println("2. Cambiar fecha");
                salida.println("3. Cambiar asiento");
                salida.println("4. Volver al inicio");
                salida.println("5. Salir");
                break;
            case SALIR:
                break;
            default:
                salida.println("Menu no disponible.");
                break;
        }
    }

    /**
     * Construye la solicitud textual que corresponde al estado y opcion elegidos.
     *
     * @param estadoActual estado desde el cual se solicita la siguiente accion.
     * @return mensaje que debe enviarse al servidor.
     */
    public String construirSolicitud(EstadoCliente estadoActual) {
        return switch (estadoActual) {
            case MENU_INICIO -> construirSolicitudMenuInicio();
            case MENU_FUNCION -> construirSolicitudMenuFuncion();
            case MENU_ASIENTO_BLOQUEADO -> construirSolicitudMenuBloqueo();
            case SALIR -> "SALIR";
            default -> "SALIR";
        };
    }

    /**
     * Construye una solicitud desde el menu inicial.
     *
     * @return mensaje de protocolo para el servidor.
     */
    private String construirSolicitudMenuInicio() {
        int opcion = leerOpcion(EstadoCliente.MENU_INICIO);
        return switch (opcion) {
            case 1 -> "SELECCIONAR_FUNCION|" + solicitarTextoObligatorio("Ingrese el ID de la funcion: ");
            case 2 -> "CAMBIAR_FECHA|" + solicitarFecha();
            default -> "SALIR";
        };
    }

    /**
     * Construye una solicitud desde el menu posterior a seleccionar una funcion.
     *
     * @return mensaje de protocolo para el servidor.
     */
    private String construirSolicitudMenuFuncion() {
        int opcion = leerOpcion(EstadoCliente.MENU_FUNCION);
        return switch (opcion) {
            case 1 -> "SELECCIONAR_ASIENTO|" + solicitarTextoObligatorio("Ingrese el asiento: ")
                    + "|" + solicitarNombreCliente(false);
            case 2 -> "CAMBIAR_FECHA|" + solicitarFecha();
            case 3 -> "CAMBIAR_FUNCION";
            case 0, 4 -> "SALIR";
            default -> "SALIR";
        };
    }

    /**
     * Construye una solicitud desde el menu posterior al bloqueo de un asiento.
     *
     * @return mensaje de protocolo para el servidor.
     */
    private String construirSolicitudMenuBloqueo() {
        int opcion = leerOpcion(EstadoCliente.MENU_ASIENTO_BLOQUEADO);
        return switch (opcion) {
            case 1 -> "CONFIRMAR_COMPRA";
            case 2 -> "CAMBIAR_FECHA|" + solicitarFecha();
            case 3 -> "CAMBIAR_ASIENTO|" + solicitarTextoObligatorio("Ingrese el nuevo asiento: ")
                    + "|" + solicitarNombreCliente(true);
            case 4 -> "VOLVER_INICIO";
            case 0, 5 -> "SALIR";
            default -> "SALIR";
        };
    }

    /**
     * Lee una opcion valida para el menu del estado indicado.
     *
     * @param estadoActual estado cuyo menu esta activo.
     * @return opcion numerica aceptada.
     */
    private int leerOpcion(EstadoCliente estadoActual) {
        while (true) {
            salida.print("Seleccione una opcion: ");
            String texto = scanner.nextLine().trim();
            try {
                int opcion = Integer.parseInt(texto);
                if (esOpcionValida(estadoActual, opcion)) {
                    return opcion;
                }
            } catch (NumberFormatException ignored) {
                // Se vuelve a pedir la opcion.
            }
            salida.println("Error: Debe seleccionar una opcion valida del menu.");
        }
    }

    /**
     * Determina si una opcion pertenece al menu activo.
     *
     * @param estadoActual estado cuyo menu esta activo.
     * @param opcion opcion numerica capturada.
     * @return {@code true} si la opcion puede procesarse.
     */
    private boolean esOpcionValida(EstadoCliente estadoActual, int opcion) {
        return switch (estadoActual) {
            case MENU_INICIO -> opcion >= 0 && opcion <= 3;
            case MENU_FUNCION -> opcion >= 0 && opcion <= 4;
            case MENU_ASIENTO_BLOQUEADO -> opcion >= 0 && opcion <= 5;
            case SALIR -> opcion == 0;
        };
    }

    /**
     * Solicita un texto obligatorio y lo valida contra entrada vacia.
     *
     * @param mensaje mensaje mostrado al usuario.
     * @return texto no vacio capturado desde consola.
     */
    private String solicitarTextoObligatorio(String mensaje) {
        while (true) {
            salida.print(mensaje);
            String texto = scanner.nextLine().trim();
            if (!texto.isEmpty()) {
                return texto;
            }
            salida.println("Error: El valor no puede estar vacio.");
        }
    }

    /**
     * Solicita una fecha textual sin validaciones de negocio.
     *
     * @return fecha capturada para enviar al servidor.
     */
    private String solicitarFecha() {
        return solicitarTextoObligatorio("Ingrese la fecha (yyyy-MM-dd): ");
    }

    /**
     * Solicita el nombre del cliente y permite reutilizar el ultimo si aplica.
     *
     * @param permitirReutilizar indica si puede reutilizarse el ultimo nombre capturado.
     * @return nombre de cliente que sera enviado al servidor.
     */
    private String solicitarNombreCliente(boolean permitirReutilizar) {
        while (true) {
            if (permitirReutilizar && !ultimoNombreCliente.isEmpty()) {
                salida.print("Ingrese el nombre del cliente [" + ultimoNombreCliente + "]: ");
                String texto = scanner.nextLine().trim();
                if (texto.isEmpty()) {
                    return ultimoNombreCliente;
                }
                ultimoNombreCliente = texto;
                return texto;
            }
            String texto = solicitarTextoObligatorio("Ingrese el nombre del cliente: ");
            ultimoNombreCliente = texto;
            return texto;
        }
    }
}
