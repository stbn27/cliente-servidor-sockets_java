package cliente;

/**
 * Punto de entrada alterno para ejecutar el cliente de cine.
 */
public class Main {

    /**
     * Delegara la ejecucion a la clase principal del cliente.
     *
     * @param args argumentos de linea de comandos.
     */
    public static void main(String[] args) {

        /* Ejecuta el cliente de cine utilizando la clase principal ClienteCine. */
        ClienteCine.main(args);
    }
}
