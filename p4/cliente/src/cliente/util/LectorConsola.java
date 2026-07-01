package cliente.util;

import java.util.Scanner;

/**
 * Lectura robusta de datos para menus por consola.
 */
public final class LectorConsola {

    private final Scanner scanner;

    public LectorConsola() {
        this.scanner = new Scanner(System.in);
    }

    /**
     * Lee un entero dentro de un rango.
     *
     * @param etiqueta texto mostrado al usuario.
     * @param minimo minimo permitido.
     * @param maximo maximo permitido.
     * @return entero valido capturado por consola.
     */
    public int leerEnteroEnRango(String etiqueta, int minimo, int maximo) {
        while (true) {
            System.out.print(etiqueta + " [" + minimo + "-" + maximo + "]: ");
            String linea = scanner.nextLine();
            try {
                int valor = Integer.parseInt(linea.trim());
                if (valor < minimo || valor > maximo) {
                    ConsolaDecoradora.advertencia("Debes elegir una opcion dentro del rango permitido.");
                    continue;
                }
                return valor;
            } catch (NumberFormatException excepcion) {
                ConsolaDecoradora.advertencia("Debes capturar un numero entero valido.");
            }
        }
    }

    /**
     * Lee un entero positivo.
     *
     * @param etiqueta nombre del dato solicitado.
     * @return entero positivo.
     */
    public int leerEnteroPositivo(String etiqueta) {
        while (true) {
            System.out.print(etiqueta + ": ");
            String linea = scanner.nextLine();
            try {
                int valor = Integer.parseInt(linea.trim());
                if (valor <= 0) {
                    ConsolaDecoradora.advertencia("El valor debe ser mayor que cero.");
                    continue;
                }
                return valor;
            } catch (NumberFormatException excepcion) {
                ConsolaDecoradora.advertencia("Debes capturar un numero entero valido.");
            }
        }
    }

    /**
     * Lee un texto obligatorio.
     *
     * @param etiqueta nombre del dato solicitado.
     * @return texto no vacio.
     */
    public String leerTextoNoVacio(String etiqueta) {
        while (true) {
            System.out.print(etiqueta + ": ");
            String linea = scanner.nextLine();
            if (linea != null && !linea.trim().isEmpty()) {
                return linea.trim();
            }
            ConsolaDecoradora.advertencia("El valor no puede estar vacio.");
        }
    }

    /**
     * Espera confirmacion del usuario antes de continuar.
     *
     * @param mensaje mensaje mostrado.
     */
    public void esperarEnter(String mensaje) {
        System.out.print(mensaje + "...");
        scanner.nextLine();
    }
}
