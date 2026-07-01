package servidor.repositorios;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import servidor.excepciones.ErrorNoEncontrado;
import servidor.excepciones.ErrorPersistencia;
import servidor.modelos.EstadisticasUsuario;
import servidor.modelos.Publicacion;
import servidor.modelos.TendenciaPais;
import servidor.persistencia.ArchivoCsvUtil;

/**
 * Acceso a datos de la categoria redes sociales.
 */
public final class RepositorioRedesSociales {

    private static final String MODULO = "REDES_SOCIALES";
    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final Path archivoTendencias;
    private final Path archivoUsuarios;
    private final Path archivoPublicaciones;

    public RepositorioRedesSociales(Path directorioBase) {
        this.archivoTendencias = directorioBase.resolve("tendencias.csv");
        this.archivoUsuarios = directorioBase.resolve("usuarios.csv");
        this.archivoPublicaciones = directorioBase.resolve("publicaciones.csv");
    }

    /**
     * Busca tendencias por pais.
     *
     * @param pais pais solicitado por el cliente.
     * @return tendencias encontradas.
     * @throws ErrorNoEncontrado si el pais no existe.
     * @throws ErrorPersistencia si falla la lectura.
     */
    public TendenciaPais buscarTendenciasPorPais(String pais) throws ErrorNoEncontrado, ErrorPersistencia {

        List<String[]> registros = ArchivoCsvUtil.leerRegistros(archivoTendencias, MODULO);

        for (String[] columnas : registros) {
            if (columnas.length >= 2 && columnas[0].equalsIgnoreCase(pais)) {
                return new TendenciaPais(columnas[0], Arrays.asList(columnas[1].split(";")));
            }
        }

        throw new ErrorNoEncontrado(
                "No se encontraron tendencias para el pais solicitado.",
                "Pais inexistente en dataset: " + pais,
                MODULO
        );
    }

    /**
     * Recupera estadisticas de un usuario.
     *
     * @param usuario alias del usuario.
     * @return estadisticas cargadas desde CSV.
     * @throws ErrorNoEncontrado si el usuario no existe.
     * @throws ErrorPersistencia si falla la lectura.
     */
    public EstadisticasUsuario buscarEstadisticasUsuario(String usuario)
            throws ErrorNoEncontrado, ErrorPersistencia {

        List<EstadisticasUsuario> usuarios = leerUsuarios();

        for (EstadisticasUsuario estadisticas : usuarios) {
            if (estadisticas.usuario().equalsIgnoreCase(usuario)) {
                return estadisticas;
            }
        }

        throw new ErrorNoEncontrado(
                "El usuario solicitado no existe en redes sociales.",
                "Usuario no encontrado: " + usuario,
                MODULO
        );
    }

    /**
     * Registra una nueva publicacion y actualiza el contador de publicaciones
     * del usuario asociado.
     *
     * @param usuario usuario autor.
     * @param pais pais donde se asocia la publicacion.
     * @param mensaje texto de la publicacion.
     * @return publicacion persistida.
     * @throws ErrorNoEncontrado si el usuario no existe.
     * @throws ErrorPersistencia si algun archivo no puede actualizarse.
     */
    public synchronized Publicacion guardarPublicacion(String usuario, String pais, String mensaje)
            throws ErrorNoEncontrado, ErrorPersistencia {

        List<EstadisticasUsuario> usuarios = leerUsuarios();
        boolean actualizado = false;
        List<EstadisticasUsuario> usuariosActualizados = new ArrayList<>();

        for (EstadisticasUsuario estadisticas : usuarios) {
            if (estadisticas.usuario().equalsIgnoreCase(usuario)) {
                usuariosActualizados.add(estadisticas.conPublicacionesIncrementadas());
                actualizado = true;
            } else {
                usuariosActualizados.add(estadisticas);
            }
        }

        if (!actualizado) {
            throw new ErrorNoEncontrado(
                    "No se puede publicar porque el usuario no existe.",
                    "Usuario desconocido: " + usuario,
                    MODULO
            );
        }

        List<Publicacion> publicaciones = leerPublicaciones();
        int siguienteId = 1;

        for (Publicacion publicacion : publicaciones) {
            if (publicacion.identificador() >= siguienteId) {
                siguienteId = publicacion.identificador() + 1;
            }
        }

        Publicacion nuevaPublicacion = new Publicacion(
                siguienteId,
                usuario,
                pais,
                mensaje,
                LocalDateTime.now().format(FORMATO_FECHA)
        );

        publicaciones.add(nuevaPublicacion);

        guardarUsuarios(usuariosActualizados);
        guardarPublicaciones(publicaciones);

        return nuevaPublicacion;
    }

    private List<EstadisticasUsuario> leerUsuarios() throws ErrorPersistencia {

        List<String[]> registros = ArchivoCsvUtil.leerRegistros(archivoUsuarios, MODULO);
        List<EstadisticasUsuario> usuarios = new ArrayList<>();

        for (String[] columnas : registros) {
            usuarios.add(new EstadisticasUsuario(
                    columnas[0],
                    Integer.parseInt(columnas[1]),
                    Integer.parseInt(columnas[2]),
                    Integer.parseInt(columnas[3]),
                    Integer.parseInt(columnas[4]),
                    columnas[5])
            );
        }

        return usuarios;
    }

    private List<Publicacion> leerPublicaciones() throws ErrorPersistencia {
        List<String[]> registros = ArchivoCsvUtil.leerRegistros(archivoPublicaciones, MODULO);
        List<Publicacion> publicaciones = new ArrayList<>();

        for (String[] columnas : registros) {
            publicaciones.add(new Publicacion(
                    Integer.parseInt(columnas[0]),
                    columnas[1],
                    columnas[2],
                    columnas[3],
                    columnas[4])
            );
        }

        return publicaciones;
    }

    private void guardarUsuarios(List<EstadisticasUsuario> usuarios) throws ErrorPersistencia {

        List<String> lineas = new ArrayList<>();
        lineas.add("usuario|seguidores|seguidos|publicaciones|interacciones|pais");

        for (EstadisticasUsuario usuario : usuarios) {
            lineas.add(usuario.usuario() + "|"
                    + usuario.seguidores() + "|"
                    + usuario.seguidos() + "|"
                    + usuario.publicaciones() + "|"
                    + usuario.interacciones() + "|"
                    + usuario.pais()
            );
        }

        ArchivoCsvUtil.escribirArchivo(archivoUsuarios, lineas, MODULO);
    }

    private void guardarPublicaciones(List<Publicacion> publicaciones) throws ErrorPersistencia {

        List<String> lineas = new ArrayList<>();
        lineas.add("id|usuario|pais|mensaje|fechaHora");

        for (Publicacion publicacion : publicaciones) {
            lineas.add(publicacion.identificador() + "|"
                    + publicacion.usuario() + "|"
                    + publicacion.pais() + "|"
                    + publicacion.mensaje() + "|"
                    + publicacion.fechaHora());
        }

        ArchivoCsvUtil.escribirArchivo(archivoPublicaciones, lineas, MODULO);
    }
}
