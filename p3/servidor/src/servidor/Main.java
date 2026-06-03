package servidor;

/**
 * Punto de entrada alterno que delega la ejecucion al servidor principal.
 */
public class Main {

    /**
     * Inicia el servidor de cine.
     *
     * @param args argumentos de linea de comandos.
     */
    public static void main(String[] args) {
        ServidorCine.main(args);
    }
}
