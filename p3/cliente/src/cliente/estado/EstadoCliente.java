package cliente.estado;

/**
 * Define los estados de navegacion que puede mostrar el cliente.
 */
public enum EstadoCliente {
    /** Estado inicial con la lista de funciones recibida del servidor. */
    MENU_INICIO("MENU_INICIO"),
    /** Estado posterior a seleccionar una funcion y mostrar sus asientos. */
    MENU_FUNCION("MENU_FUNCION"),
    /** Estado posterior al bloqueo temporal de un asiento. */
    MENU_ASIENTO_BLOQUEADO("MENU_BLOQUEO"),
    /** Estado final que indica cierre del cliente. */
    SALIR("SALIR");

    private final String identificadorMenu;

    /**
     * Crea un estado con el identificador textual usado por el protocolo.
     *
     * @param identificadorMenu identificador del menu enviado por el servidor.
     */
    EstadoCliente(String identificadorMenu) {
        this.identificadorMenu = identificadorMenu;
    }

    /**
     * Obtiene el identificador textual asociado al estado.
     *
     * @return identificador textual del menu.
     */
    public String getIdentificadorMenu() {
        return identificadorMenu;
    }

    /**
     * Convierte el identificador textual del servidor al enum del cliente.
     *
     * @param identificador identificador del menu enviado por el servidor.
     * @return estado equivalente o {@link #SALIR} si no existe coincidencia.
     */
    public static EstadoCliente desdeIdentificador(String identificador) {
        for (EstadoCliente estado : values()) {
            if (estado.identificadorMenu.equals(identificador)) {
                return estado;
            }
        }
        return SALIR;
    }
}
