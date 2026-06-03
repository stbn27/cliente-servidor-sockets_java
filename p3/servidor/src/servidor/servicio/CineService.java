package servidor.servicio;

import servidor.error.CodigoError;
import servidor.modelo.Asiento;
import servidor.modelo.BloqueoAsiento;
import servidor.modelo.Boleto;
import servidor.modelo.EstadoAsiento;
import servidor.modelo.FuncionCine;
import servidor.modelo.Pelicula;
import servidor.repositorio.ArchivoRepository;
import servidor.sesion.EstadoSesion;
import servidor.sesion.SesionCliente;
import servidor.utilidades.Protocolo;
import servidor.utilidades.TablaAsciiUtil;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Centraliza la logica del cine, el flujo guiado de la sesion y la concurrencia.
 */
public class CineService {
    private static final int SEGUNDOS_BLOQUEO = 60;
    private static final int LONGITUD_DECORADOR = 80;
    private static final DateTimeFormatter FORMATO_HORA = DateTimeFormatter.ofPattern("HH:mm:ss");

    private final ArchivoRepository repository;
    private final Map<String, Pelicula> peliculas;
    private final Map<String, FuncionCine> funciones;
    private final Map<String, Map<String, Asiento>> asientosPorFuncion;
    private final List<Boleto> boletos;
    private final Map<String, BloqueoAsiento> bloqueosActivos;
    private int ultimoBoleto;

    /**
     * Crea el servicio usando la ubicacion de datos por defecto.
     *
     * @throws IOException si ocurre un error de inicializacion.
     */
    public CineService() throws IOException {
        this(new ArchivoRepository());
    }

    /**
     * Crea el servicio usando un repositorio especifico.
     *
     * @param repository repositorio de persistencia.
     * @throws IOException si ocurre un error de inicializacion.
     */
    public CineService(ArchivoRepository repository) throws IOException {
        this.repository = repository;
        this.repository.asegurarArchivos();
        this.peliculas = repository.cargarPeliculas();
        this.funciones = repository.cargarFunciones();
        this.asientosPorFuncion = repository.cargarAsientos();
        this.boletos = repository.cargarBoletos();
        this.bloqueosActivos = new HashMap<>();
        this.ultimoBoleto = calcularUltimoBoleto();
        normalizarAsientosBloqueadosAlIniciar();
    }

    /**
     * Genera la pantalla inicial para una sesion recien conectada.<br>
     *
     * <b>synchronized</b> garantiza que solo un hilo a la vez pueda ejecutar un bloque de código o
     * un metodo protegido. Esto evita condiciones de carrera y asegura la consistencia de los datos.
     *
     * @param sesion sesion a inicializar.
     * @return respuestas que deben enviarse al cliente.
     */
    public synchronized List<String> crearPantallaInicial(SesionCliente sesion) {

        // Válida que sea una sesión se haya creado correctamente y no tenga un estado inconsistente.
        if (!sesionValida(sesion)) {
            return List.of(RespuestaServidor.error(CodigoError.SESION_INVALIDA));
        }

        // Limpia los asientos que se liberaron por no confirmar el pago antes de tiempo asignado.
        limpiarBloqueosExpirados();

        // Reinicia la sesión para asegurar un estado limpio y consistente, asignando la fecha actual del servidor.
        sesion.reiniciar(obtenerFechaActualServidor());

        // Muestra el texto del nombre del cliente y la fecha actual
        System.out.println("\n" + marcoTexto("="));
        System.out.println("Fecha seleccionada por " + sesion.getIdCliente() + ": " + sesion.getFechaSeleccionada() + ".");
        System.out.println(marcoTexto("=") + "\n");

        // Muestra las funciones de la fecha actual
        return construirPantallaFunciones(sesion, "Funciones disponibles para hoy: " + sesion.getFechaSeleccionada());
    }

    /**
     * Procesa un mensaje recibido desde un cliente dentro de su sesion.<br>
     * <b>Ejemplos:</b> de texto que se pueden recibir:
     * <ul>
     *  <li>CAMBIAR_FECHA|2026-06-05</li>
     *  <li>SELECCIONAR_FUNCION|f001</li>
     * </ul>
     *
     * @param sesion sesion del cliente.
     * @param linea mensaje recibido.
     * @return respuestas que deben enviarse al cliente.
     */
    public synchronized List<String> procesarMensaje(SesionCliente sesion, String linea) {

        // Válida que sea una sesión se haya creado correctamente y no tenga un estado inconsistente.
        if (!sesionValida(sesion)) {
            return List.of(RespuestaServidor.error(CodigoError.SESION_INVALIDA));
        }

        // Limpia los asientos que se liberaron por no confirmar el pago antes de tiempo asignado.
        limpiarBloqueosExpirados();

        // Divide el texto recibido
        String[] partes = Protocolo.dividirLinea(linea);

        // Válida que el texto no sea vacío, caso contrario lanza un error
        if (partes.length == 0) {
            return List.of(RespuestaServidor.error(CodigoError.FORMATO_MENSAJE_INVALIDO));
        }

        // Procesa el comando que se necesita
        String comando = Protocolo.comandoDe(partes);

        // Según el comando ejecuta la función respectiva
        return switch (comando) {
            case "SELECCIONAR_FUNCION" -> procesarSeleccionFuncion(sesion, partes);
            case "CAMBIAR_FECHA" -> procesarCambioFecha(sesion, partes);
            case "SELECCIONAR_ASIENTO" -> procesarSeleccionAsiento(sesion, partes);
            case "CAMBIAR_FUNCION" -> procesarCambioFuncion(sesion, partes);
            case "CAMBIAR_ASIENTO" -> procesarCambioAsiento(sesion, partes);
            case "CONFIRMAR_COMPRA" -> procesarConfirmacionCompra(sesion, partes);
            case "VOLVER_INICIO" -> procesarVolverInicio(sesion, partes);
            case "SALIR" -> procesarSalida(sesion, partes);
            default -> List.of(RespuestaServidor.error(CodigoError.COMANDO_INVALIDO));
        };
    }

    /**
     * Libera recursos temporales asociados a una sesion desconectada.
     * Por ejemplo si un usuario selecciono un asiento y finalizo la conexión
     * libera el asiento en automatico
     *
     * @param sesion sesion a cerrar.
     */
    public synchronized void cerrarSesion(SesionCliente sesion) {
        if (!sesionValida(sesion)) {
            return;
        }
        liberarBloqueoDeSesion(sesion, "Cierre de conexion");

        sesion.reiniciar(obtenerFechaActualServidor());

        // Mensaje de finalización de la conexión
        System.out.println("\n" + marcoTexto("-"));
        System.out.println("Limpieza de sesion " + sesion.getIdCliente() + ": sesion cerrada.");
        System.out.println( marcoTexto("-") + "\n");
    }

    /**
     * Genera la tabla completa de funciones cargadas para la consola del servidor.<br>
     * Esta es solo para tener una buena visualización
     *
     * @return tabla ASCII de funciones.
     */
    public synchronized String generarTablaFuncionesCargadas() {
        return TablaAsciiUtil.generarTablaFunciones(new ArrayList<>(funciones.values()), peliculas);
    }

    /**
     * Procesa la seleccion de una funcion.
     *
     * @param sesion sesion del cliente.
     * @param partes partes del mensaje recibido.
     * @return respuestas resultantes.
     */
    private List<String> procesarSeleccionFuncion(SesionCliente sesion, String[] partes) {

        // Válida que la longitud sea la esperada, caso contrario devuelve un error.
        if (!Protocolo.validarLongitud(partes, 2)) {
            return List.of(RespuestaServidor.error(CodigoError.FORMATO_MENSAJE_INVALIDO));
        }

        // Verifica que el usuario no tenga un asiento bloqueado, caso contrario devuelve un error
        // indicando que debe confirmar o cancelar su compra antes de seleccionar otra función.
        if (sesion.getEstadoActual() == EstadoSesion.ASIENTO_BLOQUEADO) {
            return List.of(RespuestaServidor.error(CodigoError.OPCION_INVALIDA,
                    "Debes cambiar de funcion, volver al inicio o confirmar la compra antes de seleccionar otra funcion."));
        }

        // Obtiene el ID de la función a seleccionar
        FuncionCine funcion = funciones.get(partes[1]);

        // Verifica que exista la función en los archivos, en caso que no, retorna un error
        if (funcion == null) {
            return List.of(RespuestaServidor.error(CodigoError.FUNCION_NO_EXISTE));
        }

        // Verifica que esa función este disponible en la fecha seleccionda(por defecto se usa la fecha actual)
        if (!funcion.getFechaComoLocalDate().equals(sesion.getFechaSeleccionada())) {
            return List.of(RespuestaServidor.error(CodigoError.OPCION_INVALIDA,
                    "La funcion " + funcion.getIdFuncion() + " - " + " no esta disponible en la fecha seleccionada."));
        }

        // Se conserva en memoria la función seleccionada(propia de la sesión del cliente)
        sesion.seleccionarFuncion(funcion.getIdFuncion());

        //Muestra en la terminal del servidor el texto de la función seleccionada
        System.out.println("\n" + marcoTexto("*"));
        System.out.println("Funcion seleccionada por " + sesion.getIdCliente() + ": " + funcion.getIdFuncion() + ".");
        System.out.println(marcoTexto("*") + "\n");

        // Muestra los asientos disponibles de esa función
        return construirPantallaFuncion(sesion);
    }

    /**
     * Procesa el cambio de fecha solicitado por el cliente.<br>
     * Inicialmente se selecciona la fecha actual, pero el usuario puede cambiar le fecha usando una a futuro.
     *
     * @param sesion sesion del cliente.
     * @param partes partes del mensaje recibido.
     * @return respuestas resultantes.
     */
    private List<String> procesarCambioFecha(SesionCliente sesion, String[] partes) {

        // Valida que el texto que se envío del cliente este correcto ["CAMBIAR_FECHA","2026-06-05"]
        if (!Protocolo.validarLongitud(partes, 2)) {
            return List.of(RespuestaServidor.error(CodigoError.FORMATO_MENSAJE_INVALIDO));
        }

        // Válida el formato de la nueva fecha
        LocalDate fechaSolicitada;
        try {
            fechaSolicitada = LocalDate.parse(partes[1]);
        } catch (DateTimeParseException excepcion) {
            return List.of(RespuestaServidor.error(CodigoError.FECHA_INVALIDA,
                    "La fecha enviada no tiene un formato valido. Usa AAAA-MM-DD."));
        }

        // Válida que la fecha sea igual o posterior a la fecha actual del servidor, caso contrario devuelve un error
        // indicando que no se pueden seleccionar fechas pasadas.
        if (fechaSolicitada.isBefore(obtenerFechaActualServidor())) {
            return List.of(RespuestaServidor.error(CodigoError.FECHA_INVALIDA));
        }

        // Libera los recusor seleccionado por el cliente
        liberarBloqueoDeSesion(sesion, "Cambio de fecha");

        // Reinicia la sesión para asegurar un estado limpio y consistente, asignando la nueva fecha seleccionada.
        sesion.reiniciar(fechaSolicitada);

        // Muestra en la terminal del servidor el texto de la fecha seleccionada
        System.out.println("\n" + marcoTexto("="));
        System.out.println("Fecha seleccionada por " + sesion.getIdCliente() + ": " + sesion.getFechaSeleccionada() + ".");
        System.out.println(marcoTexto("=") + "\n");

        // Muestra las funciones disponibles para la fecha seleccionada.
        return construirPantallaFunciones(sesion, "Funciones disponibles para la fecha: " + sesion.getFechaSeleccionada());
    }


    /**
     * Procesa la seleccion inicial de un asiento dentro de la funcion elegida.
     *
     * @param sesion sesion del cliente.
     * @param partes partes del mensaje recibido.
     * @return respuestas resultantes.
     */
    private List<String> procesarSeleccionAsiento(SesionCliente sesion, String[] partes) {
        if (!Protocolo.validarLongitud(partes, 3)) {
            return List.of(RespuestaServidor.error(CodigoError.FORMATO_MENSAJE_INVALIDO));
        }
        if (!sesion.tieneFuncionSeleccionada()) {
            return List.of(RespuestaServidor.error(CodigoError.FUNCION_NO_SELECCIONADA));
        }
        if (sesion.getEstadoActual() == EstadoSesion.ASIENTO_BLOQUEADO) {
            return List.of(RespuestaServidor.error(CodigoError.OPCION_INVALIDA,
                    "Ya existe un asiento bloqueado. Usa CAMBIAR_ASIENTO para modificarlo."));
        }
        String nombreCliente = partes[2].trim();
        if (nombreCliente.isEmpty()) {
            return List.of(RespuestaServidor.error(CodigoError.FORMATO_MENSAJE_INVALIDO,
                    "Debes indicar un nombre de cliente al seleccionar un asiento."));
        }
        return bloquearAsientoParaSesion(sesion, partes[1].trim(), nombreCliente);
    }

    /**
     * Procesa el cambio de funcion dentro de la fecha actual.
     *
     * @param sesion sesion del cliente.
     * @param partes partes del mensaje recibido.
     * @return respuestas resultantes.
     */
    private List<String> procesarCambioFuncion(SesionCliente sesion, String[] partes) {
        if (!Protocolo.validarLongitud(partes, 1)) {
            return List.of(RespuestaServidor.error(CodigoError.FORMATO_MENSAJE_INVALIDO));
        }
        if (sesion.getFechaSeleccionada() == null) {
            return List.of(RespuestaServidor.error(CodigoError.SESION_INVALIDA));
        }
        liberarBloqueoDeSesion(sesion, "Cambio de funcion");
        sesion.limpiarFuncionSeleccionada();
        System.out.println("Limpieza de sesion " + sesion.getIdCliente() + ": cambio de funcion.");
        return construirPantallaFunciones(sesion,
                "Funciones disponibles para la fecha: " + sesion.getFechaSeleccionada());
    }

    /**
     * Procesa el cambio de asiento para una funcion ya seleccionada.
     *
     * @param sesion sesion del cliente.
     * @param partes partes del mensaje recibido.
     * @return respuestas resultantes.
     */
    private List<String> procesarCambioAsiento(SesionCliente sesion, String[] partes) {
        if (!Protocolo.validarLongitud(partes, 3)) {
            return List.of(RespuestaServidor.error(CodigoError.FORMATO_MENSAJE_INVALIDO));
        }
        if (!sesion.tieneFuncionSeleccionada()) {
            return List.of(RespuestaServidor.error(CodigoError.FUNCION_NO_SELECCIONADA));
        }
        if (!sesion.tieneAsientoBloqueado()) {
            return List.of(RespuestaServidor.error(CodigoError.ASIENTO_NO_SELECCIONADO));
        }
        String nombreCliente = partes[2].trim();
        if (nombreCliente.isEmpty()) {
            return List.of(RespuestaServidor.error(CodigoError.FORMATO_MENSAJE_INVALIDO,
                    "Debes indicar un nombre de cliente al cambiar de asiento."));
        }
        liberarBloqueoDeSesion(sesion, "Cambio de asiento");
        return bloquearAsientoParaSesion(sesion, partes[1].trim(), nombreCliente);
    }

    /**
     * Procesa la confirmacion final de una compra.
     *
     * @param sesion sesion del cliente.
     * @param partes partes del mensaje recibido.
     * @return respuestas resultantes.
     */
    private List<String> procesarConfirmacionCompra(SesionCliente sesion, String[] partes) {
        if (!Protocolo.validarLongitud(partes, 1)) {
            return List.of(RespuestaServidor.error(CodigoError.FORMATO_MENSAJE_INVALIDO));
        }
        if (!sesion.tieneFuncionSeleccionada()) {
            return List.of(RespuestaServidor.error(CodigoError.FUNCION_NO_SELECCIONADA));
        }
        if (!sesion.tieneAsientoBloqueado()) {
            return List.of(RespuestaServidor.error(CodigoError.ASIENTO_NO_SELECCIONADO));
        }

        LocalDateTime ahora = LocalDateTime.now().withNano(0);
        if (sesion.getHoraExpiracionBloqueo() == null || !sesion.getHoraExpiracionBloqueo().isAfter(ahora)) {
            liberarBloqueoDeSesion(sesion, "Bloqueo expirado al confirmar");
            sesion.reiniciar(obtenerFechaActualServidor());
            System.out.println("Limpieza de sesion " + sesion.getIdCliente() + ": expiracion de bloqueo.");
            List<String> respuestas = new ArrayList<>();
            respuestas.add(RespuestaServidor.error(CodigoError.TIEMPO_BLOQUEO_EXPIRADO));
            respuestas.addAll(construirPantallaFunciones(sesion,
                    "Funciones disponibles para hoy: " + sesion.getFechaSeleccionada()));
            return respuestas;
        }

        String idFuncion = sesion.getIdFuncionSeleccionada();
        String asientoId = sesion.getAsientoBloqueadoActual();
        Map<String, Asiento> asientosFuncion = asientosPorFuncion.get(idFuncion);
        if (asientosFuncion == null || !asientosFuncion.containsKey(asientoId)) {
            return reiniciarPorSesionInvalida(sesion,
                    "El asiento asociado a la sesion ya no existe en la funcion seleccionada.");
        }

        String clave = claveBloqueo(idFuncion, asientoId);
        BloqueoAsiento bloqueo = bloqueosActivos.get(clave);
        if (bloqueo == null || !bloqueo.getIdCliente().equals(sesion.getIdCliente())) {
            return reiniciarPorSesionInvalida(sesion,
                    "El bloqueo temporal ya no pertenece a la sesion actual.");
        }

        Asiento asiento = asientosFuncion.get(asientoId);
        EstadoAsiento estadoAnterior = asiento.getEstado();
        asiento.setEstado(EstadoAsiento.VENDIDO);
        bloqueosActivos.remove(clave);

        FuncionCine funcion = funciones.get(idFuncion);
        Pelicula pelicula = funcion != null ? peliculas.get(funcion.getIdPelicula()) : null;
        String idBoleto = generarIdBoleto();
        LocalDateTime fechaHoraCompra = LocalDateTime.now().withNano(0);
        Boleto boleto = new Boleto(idBoleto, idFuncion, asientoId, sesion.getNombreCliente(),
                fechaHoraCompra.toLocalDate().toString(), fechaHoraCompra.toLocalTime().format(FORMATO_HORA));
        boletos.add(boleto);

        try {
            repository.guardarAsientos(asientosPorFuncion);
            repository.guardarBoletos(boletos);
        } catch (IOException excepcion) {
            asiento.setEstado(estadoAnterior);
            bloqueosActivos.put(clave, bloqueo);
            boletos.remove(boletos.size() - 1);
            return List.of(RespuestaServidor.error(CodigoError.ERROR_ARCHIVO));
        }

        sesion.marcarCompraConfirmada();
        System.out.println("Asiento vendido: funcion " + idFuncion + ", asiento " + asientoId
                + ", cliente " + sesion.getIdCliente() + ".");

        List<String> respuestas = new ArrayList<>();
        respuestas.add(RespuestaServidor.ok(
                "BOLETO_GENERADO",
                idBoleto,
                "Asiento reservado correctamente.",
                "Pelicula: " + (pelicula != null ? pelicula.getTitulo() : "Desconocida"),
                "Sala: " + (funcion != null ? funcion.getSala() : "N/D"),
                "Fecha: " + (funcion != null ? funcion.getFecha() : "N/D"),
                "Hora: " + (funcion != null ? funcion.getHora() : "N/D"),
                "Asiento: " + asientoId,
                "Cliente: " + sesion.getNombreCliente()
        ));
        sesion.reiniciar(obtenerFechaActualServidor());
        System.out.println("Limpieza de sesion " + sesion.getIdCliente() + ": compra confirmada.");
        respuestas.addAll(construirPantallaFunciones(sesion,
                "Funciones disponibles para hoy: " + sesion.getFechaSeleccionada()));
        return respuestas;
    }

    /**
     * Procesa el retorno al inicio del flujo.
     *
     * @param sesion sesion del cliente.
     * @param partes partes del mensaje recibido.
     * @return respuestas resultantes.
     */
    private List<String> procesarVolverInicio(SesionCliente sesion, String[] partes) {
        if (!Protocolo.validarLongitud(partes, 1)) {
            return List.of(RespuestaServidor.error(CodigoError.FORMATO_MENSAJE_INVALIDO));
        }
        liberarBloqueoDeSesion(sesion, "Volver al inicio");
        sesion.reiniciar(obtenerFechaActualServidor());
        System.out.println("Limpieza de sesion " + sesion.getIdCliente() + ": regreso al inicio.");
        return construirPantallaFunciones(sesion, "Funciones disponibles para hoy: " + sesion.getFechaSeleccionada());
    }

    /**
     * Procesa la salida del cliente y libera sus recursos temporales.
     *
     * @param sesion sesion del cliente.
     * @param partes partes del mensaje recibido.
     * @return respuestas resultantes.
     */
    private List<String> procesarSalida(SesionCliente sesion, String[] partes) {
        if (!Protocolo.validarLongitud(partes, 1)) {
            return List.of(RespuestaServidor.error(CodigoError.FORMATO_MENSAJE_INVALIDO));
        }
        liberarBloqueoDeSesion(sesion, "Salida solicitada");
        sesion.reiniciar(obtenerFechaActualServidor());
        System.out.println("Limpieza de sesion " + sesion.getIdCliente() + ": salida del cliente.");
        return List.of(RespuestaServidor.ok("DESCONEXION", "Cliente desconectado."));
    }

    /**
     * Intenta bloquear un asiento para la sesion actual.
     *
     * @param sesion sesion del cliente.
     * @param asientoId identificador del asiento.
     * @param nombreCliente nombre del cliente.
     * @return respuestas resultantes.
     */
    private List<String> bloquearAsientoParaSesion(SesionCliente sesion, String asientoId, String nombreCliente) {
        String idFuncion = sesion.getIdFuncionSeleccionada();
        Map<String, Asiento> asientosFuncion = asientosPorFuncion.get(idFuncion);
        if (asientosFuncion == null) {
            return List.of(RespuestaServidor.error(CodigoError.FUNCION_NO_EXISTE));
        }
        Asiento asiento = asientosFuncion.get(asientoId);
        if (asiento == null) {
            return List.of(RespuestaServidor.error(CodigoError.ASIENTO_NO_EXISTE));
        }
        if (asiento.getEstado() == EstadoAsiento.VENDIDO) {
            return List.of(RespuestaServidor.error(CodigoError.ASIENTO_VENDIDO,
                    "El asiento " + asientoId + " ya fue vendido."));
        }

        String clave = claveBloqueo(idFuncion, asientoId);
        BloqueoAsiento bloqueoExistente = bloqueosActivos.get(clave);
        LocalDateTime ahora = LocalDateTime.now().withNano(0);
        if (bloqueoExistente != null && bloqueoExistente.haExpirado(ahora)) {
            liberarBloqueoInterno(idFuncion, asientoId, bloqueoExistente, "Bloqueo expirado detectado en nueva solicitud");
            bloqueoExistente = null;
        }
        if (bloqueoExistente != null && !bloqueoExistente.getIdCliente().equals(sesion.getIdCliente())) {
            return List.of(RespuestaServidor.error(CodigoError.ASIENTO_BLOQUEADO,
                    "El asiento " + asientoId + " esta apartado temporalmente por otro cliente."));
        }

        EstadoAsiento estadoAnterior = asiento.getEstado();
        LocalDateTime expiracion = ahora.plusSeconds(SEGUNDOS_BLOQUEO);
        BloqueoAsiento nuevoBloqueo = new BloqueoAsiento(idFuncion, asientoId, sesion.getIdCliente(), nombreCliente,
                ahora, expiracion);
        asiento.setEstado(EstadoAsiento.BLOQUEADO);
        bloqueosActivos.put(clave, nuevoBloqueo);

        try {
            repository.guardarAsientos(asientosPorFuncion);
        } catch (IOException excepcion) {
            asiento.setEstado(estadoAnterior);
            bloqueosActivos.remove(clave);
            return List.of(RespuestaServidor.error(CodigoError.ERROR_ARCHIVO));
        }

        sesion.registrarBloqueo(asientoId, nombreCliente, expiracion);
        System.out.println("Asiento bloqueado: funcion " + idFuncion + ", asiento " + asientoId
                + ", cliente " + sesion.getIdCliente() + ", expira " + expiracion + ".");

        List<String> respuestas = new ArrayList<>();
        respuestas.add(RespuestaServidor.ok(
                "ASIENTO_BLOQUEADO",
                idFuncion,
                asientoId,
                expiracion.toString(),
                "Tienes 1 minuto para confirmar tu compra, antes de las "
                        + expiracion.toLocalTime().format(FORMATO_HORA) + "."
        ));
        respuestas.addAll(construirPantallaBloqueo(sesion));
        return respuestas;
    }

    /**
     * Construye la pantalla inicial o de funciones para la fecha de la sesion.
     *
     * @param sesion sesion del cliente.
     * @param encabezado encabezado principal de la pantalla.
     * @return lista con la respuesta de pantalla.
     */
    private List<String> construirPantallaFunciones(SesionCliente sesion, String encabezado) {
        List<FuncionCine> funcionesDelDia = obtenerFuncionesPorFecha(sesion.getFechaSeleccionada());
        String contenido = construirContenidoFunciones(funcionesDelDia, encabezado);
        return List.of(RespuestaServidor.ok("PANTALLA", contenido, Protocolo.MENU_INICIO));
    }

    /**
     * Construye la pantalla de asientos de la funcion seleccionada.
     *
     * @param sesion sesion del cliente.
     * @return lista con la respuesta de pantalla.
     */
    private List<String> construirPantallaFuncion(SesionCliente sesion) {
        FuncionCine funcion = funciones.get(sesion.getIdFuncionSeleccionada());
        String titulo = obtenerTituloFuncion(funcion);
        String contenido = construirContenidoAsientos(
                obtenerAsientosPorFuncion(sesion.getIdFuncionSeleccionada()),
                "Asientos disponibles para " + titulo + " (" + sesion.getIdFuncionSeleccionada() + "):",
                construirMenuFuncion()
        );
        return List.of(RespuestaServidor.ok("PANTALLA", contenido, Protocolo.MENU_FUNCION));
    }

    /**
     * Construye la pantalla posterior al bloqueo de un asiento.
     *
     * @param sesion sesion del cliente.
     * @return lista con la respuesta de pantalla.
     */
    private List<String> construirPantallaBloqueo(SesionCliente sesion) {
        FuncionCine funcion = funciones.get(sesion.getIdFuncionSeleccionada());
        String titulo = obtenerTituloFuncion(funcion);
        String contenido = construirContenidoAsientos(
                obtenerAsientosPorFuncion(sesion.getIdFuncionSeleccionada()),
                "Asiento bloqueado para " + titulo + " (" + sesion.getIdFuncionSeleccionada() + "):",
                construirMenuBloqueo()
        );
        return List.of(RespuestaServidor.ok("PANTALLA", contenido, Protocolo.MENU_BLOQUEO));
    }

    /**
     * Construye el contenido textual para una pantalla de funciones.
     *
     * @param funcionesDelDia funciones de la fecha seleccionada.
     * @param encabezado encabezado principal.
     * @return contenido legible de la pantalla.
     */
    private String construirContenidoFunciones(List<FuncionCine> funcionesDelDia, String encabezado) {
        StringBuilder contenido = new StringBuilder();
        contenido.append(encabezado).append('\n').append('\n');
        if (funcionesDelDia.isEmpty()) {
            contenido.append("No hay funciones disponibles para la fecha seleccionada.");
            System.err.println("[INFO]\tRevisar el archivo: data/funciones.txt y agregar fechas para la fecha seleccionada\n");
        } else {
            contenido.append(TablaAsciiUtil.generarTablaFunciones(funcionesDelDia, peliculas));
        }
        contenido.append('\n').append('\n').append(construirMenuInicio());
        return contenido.toString();
    }

    /**
     * Construye el contenido textual para una pantalla de asientos.
     *
     * @param asientos asientos de la funcion.
     * @param encabezado encabezado principal.
     * @param menu menu a mostrar al final.
     * @return contenido legible de la pantalla.
     */
    private String construirContenidoAsientos(List<Asiento> asientos, String encabezado, String menu) {
        StringBuilder contenido = new StringBuilder();
        contenido.append(encabezado).append('\n').append('\n');
        contenido.append(TablaAsciiUtil.generarTablaAsientos(asientos));
        contenido.append('\n').append('\n').append(menu);
        return contenido.toString();
    }

    /**
     * Construye el menu textual del estado inicial.
     *
     * @return menu textual.
     */
    private String construirMenuInicio() {
        return "1. Seleccionar funcion\n2. Cambiar fecha\n0. Salir";
    }

    /**
     * Construye el menu textual posterior a seleccionar una funcion.
     *
     * @return menu textual.
     */
    private String construirMenuFuncion() {
        return "1. Seleccionar asiento\n2. Cambiar fecha\n3. Cambiar funcion\n0. Salir";
    }

    /**
     * Construye el menu textual posterior al bloqueo de un asiento.
     *
     * @return menu textual.
     */
    private String construirMenuBloqueo() {
        return "1. Confirmar compra\n2. Cambiar fecha\n3. Cambiar asiento\n4. Volver al inicio\n5. Salir";
    }

    /**
     * Obtiene las funciones disponibles para una fecha concreta.
     *
     * @param fecha fecha de consulta.
     * @return lista ordenada de funciones.
     */
    private List<FuncionCine> obtenerFuncionesPorFecha(LocalDate fecha) {
        List<FuncionCine> lista = new ArrayList<>();
        for (FuncionCine funcion : funciones.values()) {
            if (funcion.getFechaComoLocalDate().equals(fecha)) {
                lista.add(funcion);
            }
        }
        lista.sort(Comparator.comparing(FuncionCine::getHoraComoLocalTime).thenComparing(FuncionCine::getIdFuncion));
        return lista;
    }

    /**
     * Obtiene los asientos de una funcion ordenados por identificador.
     *
     * @param idFuncion identificador de la funcion.
     * @return lista ordenada de asientos.
     */
    private List<Asiento> obtenerAsientosPorFuncion(String idFuncion) {
        List<Asiento> lista = new ArrayList<>();
        Map<String, Asiento> asientosFuncion = asientosPorFuncion.get(idFuncion);
        if (asientosFuncion != null) {
            lista.addAll(asientosFuncion.values());
        }
        lista.sort(Comparator.comparing(Asiento::getNumero));
        return lista;
    }

    /**
     * Libera el asiento asociado a una sesion, si existe.
     *
     * @param sesion sesion a limpiar.
     * @param motivo motivo de la liberacion.
     */
    private void liberarBloqueoDeSesion(SesionCliente sesion, String motivo) {

        // Verifica si el cliente tiene alguna función seleccionada o algún asiento reservado
        if (!sesion.tieneFuncionSeleccionada() || !sesion.tieneAsientoBloqueado()) {
            sesion.limpiarBloqueo();
            return;
        }

        String idFuncion = sesion.getIdFuncionSeleccionada();
        String asientoId = sesion.getAsientoBloqueadoActual();
        String clave = claveBloqueo(idFuncion, asientoId);

        BloqueoAsiento bloqueo = bloqueosActivos.get(clave);
        if (bloqueo != null && bloqueo.getIdCliente().equals(sesion.getIdCliente())) {
            liberarBloqueoInterno(idFuncion, asientoId, bloqueo, motivo);
        }
        sesion.limpiarBloqueo();

        // Impresión en consola de liberación de asientos/recursos
        System.out.println("\n" + marcoTexto("-"));
        System.out.println("Limpieza de sesion " + sesion.getIdCliente() + ": " + motivo + ".");
        System.out.println(marcoTexto("-") + "\n");
    }

    /**
     * Libera internamente un bloqueo activo y persiste el nuevo estado.
     *
     * @param idFuncion identificador de la funcion.
     * @param asientoId identificador del asiento.
     * @param bloqueo bloqueo que se liberara.
     * @param motivo motivo de liberacion para el log.
     */
    private void liberarBloqueoInterno(String idFuncion, String asientoId, BloqueoAsiento bloqueo, String motivo) {
        Map<String, Asiento> asientosFuncion = asientosPorFuncion.get(idFuncion);
        if (asientosFuncion == null) {
            bloqueosActivos.remove(claveBloqueo(idFuncion, asientoId));
            return;
        }
        Asiento asiento = asientosFuncion.get(asientoId);
        if (asiento == null) {
            bloqueosActivos.remove(claveBloqueo(idFuncion, asientoId));
            return;
        }
        EstadoAsiento estadoAnterior = asiento.getEstado();
        asiento.setEstado(EstadoAsiento.DISPONIBLE);
        bloqueosActivos.remove(claveBloqueo(idFuncion, asientoId));
        try {
            repository.guardarAsientos(asientosPorFuncion);
        } catch (IOException excepcion) {
            asiento.setEstado(estadoAnterior);
            bloqueosActivos.put(claveBloqueo(idFuncion, asientoId), bloqueo);
            System.out.println("No se pudo persistir la liberacion del asiento " + asientoId + ": "
                    + excepcion.getMessage());
            return;
        }
        System.out.println("Asiento liberado: funcion " + idFuncion + ", asiento " + asientoId
                + ", motivo: " + motivo + ".");
    }

    /**
     * Limpia todos los bloqueos expirados que sigan registrados en memoria.
     * Cuando un usuario selecciona una asiento temporalmente se bloquea durante <b>60 segundos</b> para que pueda confirmar su compra.
     * Si el usuario no confirma en ese tiempo, el bloqueo expira y el asiento vuelve a estar disponible para otros clientes.
     */
    private void limpiarBloqueosExpirados() {
        LocalDateTime ahora = LocalDateTime.now().withNano(0);
        boolean huboCambios = false;
        Iterator<Map.Entry<String, BloqueoAsiento>> iterador = bloqueosActivos.entrySet().iterator();
        while (iterador.hasNext()) {
            Map.Entry<String, BloqueoAsiento> entrada = iterador.next();
            BloqueoAsiento bloqueo = entrada.getValue();
            if (!bloqueo.haExpirado(ahora)) {
                continue;
            }
            Map<String, Asiento> asientosFuncion = asientosPorFuncion.get(bloqueo.getIdFuncion());
            if (asientosFuncion != null) {
                Asiento asiento = asientosFuncion.get(bloqueo.getNumeroAsiento());
                if (asiento != null && asiento.getEstado() == EstadoAsiento.BLOQUEADO) {
                    asiento.setEstado(EstadoAsiento.DISPONIBLE);
                    huboCambios = true;
                }
            }
            iterador.remove();
            System.out.println("Bloqueo expirado: funcion " + bloqueo.getIdFuncion() + ", asiento "
                    + bloqueo.getNumeroAsiento() + ", cliente " + bloqueo.getIdCliente() + ".");
        }
        if (huboCambios) {
            try {
                repository.guardarAsientos(asientosPorFuncion);
            } catch (IOException excepcion) {
                System.out.println("No se pudieron persistir los bloqueos expirados: " + excepcion.getMessage());
            }
        }
    }

    /**
     * Reinicia una sesion cuando su estado ya no es consistente.
     *
     * @param sesion sesion a reiniciar.
     * @param mensaje mensaje concreto del error.
     * @return respuestas de error y pantalla inicial.
     */
    private List<String> reiniciarPorSesionInvalida(SesionCliente sesion, String mensaje) {
        liberarBloqueoDeSesion(sesion, "Sesion invalida detectada");
        sesion.reiniciar(obtenerFechaActualServidor());
        List<String> respuestas = new ArrayList<>();
        respuestas.add(RespuestaServidor.error(CodigoError.SESION_INVALIDA, mensaje));
        respuestas.addAll(construirPantallaFunciones(sesion,
                "Funciones disponibles para hoy: " + sesion.getFechaSeleccionada()));
        return respuestas;
    }

    /**
     * Genera el siguiente identificador secuencial de boleto.
     *
     * @return identificador del nuevo boleto.
     */
    private String generarIdBoleto() {
        ultimoBoleto++;
        return String.format("B%04d", ultimoBoleto);
    }

    /**
     * Calcula el ultimo identificador numerico de boleto ya almacenado.
     *
     * @return valor numerico mas alto encontrado.
     */
    private int calcularUltimoBoleto() {
        int maximo = 0;
        for (Boleto boleto : boletos) {
            String id = boleto.getIdBoleto();
            if (id != null && id.matches("B\\d{4,}")) {
                maximo = Math.max(maximo, Integer.parseInt(id.substring(1)));
            }
        }
        return maximo;
    }

    /**
     * Restablece a disponible cualquier asiento bloqueado que haya quedado persistido.
     *
     * @throws IOException si ocurre un error al persistir la normalizacion.
     */
    private void normalizarAsientosBloqueadosAlIniciar() throws IOException {
        boolean huboCambios = false;
        for (Map<String, Asiento> asientosFuncion : asientosPorFuncion.values()) {
            for (Asiento asiento : asientosFuncion.values()) {
                if (asiento.getEstado() == EstadoAsiento.BLOQUEADO) {
                    asiento.setEstado(EstadoAsiento.DISPONIBLE);
                    huboCambios = true;
                }
            }
        }
        if (huboCambios) {
            repository.guardarAsientos(asientosPorFuncion);
        }
    }

    /**
     * Construye la clave interna de un bloqueo.
     *
     * @param idFuncion identificador de la funcion.
     * @param asientoId identificador del asiento.
     * @return clave unica del bloqueo.
     */
    private String claveBloqueo(String idFuncion, String asientoId) {
        return idFuncion + "|" + asientoId;
    }

    /**
     * Obtiene un titulo descriptivo de la funcion seleccionada.
     *
     * @param funcion funcion a describir.
     * @return titulo descriptivo.
     */
    private String obtenerTituloFuncion(FuncionCine funcion) {
        if (funcion == null) {
            return "funcion desconocida";
        }
        Pelicula pelicula = peliculas.get(funcion.getIdPelicula());
        String titulo = pelicula != null ? pelicula.getTitulo() : "Desconocida";
        return titulo + " - " + funcion.getSala() + " - " + funcion.getFecha() + " " + funcion.getHora();
    }

    /**
     * Obtiene la fecha actual del servidor.
     *
     * @return fecha actual del servidor.
     */
    private LocalDate obtenerFechaActualServidor() {
        return LocalDate.now();
    }

    /**
     * Verifica que la sesion recibida sea utilizable.
     *
     * @param sesion sesion a evaluar.
     * @return {@code true} si la sesion es valida.
     */
    private boolean sesionValida(SesionCliente sesion) {
        return sesion != null && sesion.getIdCliente() != null && !sesion.getIdCliente().isBlank();
    }

    /**
     * Repite un caracter tantas veces como se indique en la constante LONGITUD_DECORADOR
     * @param c Caracter a repetir
     * @return cadena con el caracter repetido
     */
    private String marcoTexto(String c){
        c = c.substring(0, 1);
        return c.repeat(LONGITUD_DECORADOR);
    }
}
