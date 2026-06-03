package servidor.error;

/**
 * Enum con los codigos y mensajes de error propios del servidor.
 */
public enum CodigoError {
    /** El comando enviado no forma parte del protocolo. */
    COMANDO_INVALIDO("ERR_COMANDO_INVALIDO", "El comando recibido no es valido."),
    /** La cantidad o estructura de parametros no coincide con el protocolo. */
    FORMATO_MENSAJE_INVALIDO("ERR_FORMATO_INVALIDO", "El formato del mensaje es invalido."),
    /** La fecha solicitada es anterior a la fecha actual del servidor o no es interpretable. */
    FECHA_INVALIDA("ERR_FECHA_INVALIDA", "No se puede consultar una fecha anterior a la fecha actual del servidor."),
    /** La funcion solicitada no existe. */
    FUNCION_NO_EXISTE("ERR_FUNCION_NO_EXISTE", "La funcion solicitada no existe."),
    /** El flujo actual aun no tiene una funcion seleccionada. */
    FUNCION_NO_SELECCIONADA("ERR_FUNCION_NO_SELECCIONADA", "Debes seleccionar una funcion antes de continuar."),
    /** El asiento solicitado no existe. */
    ASIENTO_NO_EXISTE("ERR_ASIENTO_NO_EXISTE", "El asiento solicitado no existe."),
    /** El flujo actual aun no tiene un asiento seleccionado. */
    ASIENTO_NO_SELECCIONADO("ERR_ASIENTO_NO_SELECCIONADO", "Debes seleccionar un asiento antes de continuar."),
    /** El asiento ya esta apartado temporalmente por otro cliente. */
    ASIENTO_BLOQUEADO("ERR_ASIENTO_BLOQUEADO", "El asiento esta apartado temporalmente por otro cliente."),
    /** El asiento ya fue vendido. */
    ASIENTO_VENDIDO("ERR_ASIENTO_VENDIDO", "El asiento ya fue vendido."),
    /** El asiento no pertenece al bloqueo actual del cliente. */
    ASIENTO_NO_BLOQUEADO_POR_CLIENTE("ERR_ASIENTO_NO_BLOQUEADO_POR_CLIENTE",
            "El asiento no esta bloqueado por este cliente."),
    /** El tiempo maximo del bloqueo temporal ya expiro. */
    TIEMPO_BLOQUEO_EXPIRADO("ERR_TIEMPO_BLOQUEO_EXPIRADO",
            "El tiempo se ha agotado. Por favor inicia nuevamente."),
    /** La sesion no se encuentra en un estado utilizable. */
    SESION_INVALIDA("ERR_SESION_INVALIDA", "La sesion del cliente no es valida."),
    /** La operacion no corresponde con el estado actual del flujo. */
    OPCION_INVALIDA("ERR_OPCION_INVALIDA", "La opcion solicitada no es valida para el estado actual."),
    /** Ocurrio un problema al leer o escribir los archivos de datos. */
    ERROR_ARCHIVO("ERR_ERROR_ARCHIVO", "Ocurrio un error al acceder a los archivos."),
    /** Ocurrio un fallo inesperado dentro del servidor. */
    ERROR_INTERNO("ERR_ERROR_INTERNO", "Ocurrio un error interno en el servidor.");

    private final String codigo;
    private final String mensaje;

    /**
     * Crea un codigo de error con su mensaje base.
     *
     * @param codigo codigo textual enviado al cliente.
     * @param mensaje mensaje base asociado al error.
     */
    CodigoError(String codigo, String mensaje) {
        this.codigo = codigo;
        this.mensaje = mensaje;
    }

    /**
     * Obtiene el codigo textual del error.
     *
     * @return codigo textual.
     */
    public String getCodigo() {
        return codigo;
    }

    /**
     * Obtiene el mensaje base del error.
     *
     * @return mensaje base.
     */
    public String getMensaje() {
        return mensaje;
    }
}
