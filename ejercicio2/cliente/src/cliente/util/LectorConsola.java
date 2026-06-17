package cliente.util;

import java.util.Scanner;

/**
 * Encapsula la lectura y validación de datos por consola.
 */
public class LectorConsola {

    private final Scanner scanner;

    public LectorConsola() {
        this.scanner = new Scanner(System.in);
    }

    public int leerEntero(String mensaje) {
        while (true) {
            System.out.print(mensaje);
            String entrada = scanner.nextLine().trim();

            try {
                return Integer.parseInt(entrada);
            } catch (NumberFormatException e) {
                DecoradorConsola.advertencia("Debes capturar un número entero válido.");
            }
        }
    }


    public double leerDouble(String mensaje) {
        while (true) {
            System.out.print(mensaje);
            String entrada = scanner.nextLine().trim().replace(",", ".");

            try {
                return Double.parseDouble(entrada);
            } catch (NumberFormatException e) {
                DecoradorConsola.advertencia("Debes capturar un número decimal válido.");
            }
        }
    }

    public void cerrar() {
        scanner.close();
    }
}