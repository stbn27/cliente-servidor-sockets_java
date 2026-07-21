package server.remote;

import common.dto.EstadoServidor;
import common.exception.*;
import common.model.*;
import common.remote.TableroNoticiasRemote;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import server.database.DatabaseManager;
import server.service.AutenticacionService;
import server.service.NoticiaService;
import server.service.SesionService;

import java.rmi.RemoteException;
import java.time.Instant;
import java.util.List;

public final class TableroNoticiasRemoteImpl implements TableroNoticiasRemote {
    private static final Logger LOGGER =
            LoggerFactory.getLogger(TableroNoticiasRemoteImpl.class);

    private final DatabaseManager database;
    private final AutenticacionService autenticacion;
    private final NoticiaService noticias;
    private final SesionService sesiones;

    public TableroNoticiasRemoteImpl(DatabaseManager database,
                                     AutenticacionService autenticacion,
                                     NoticiaService noticias,
                                     SesionService sesiones) {
        this.database = database;
        this.autenticacion = autenticacion;
        this.noticias = noticias;
        this.sesiones = sesiones;
    }

    @Override
    public EstadoServidor verificarEstado() throws RemoteException {
        try {
            boolean disponible = database.isAvailable();
            return new EstadoServidor(true, disponible,
                    disponible ? "Servidor disponible."
                            : "Servidor activo, base de datos no disponible.",
                    Instant.now());
        } catch (RuntimeException error) {
            throw falloInesperado("verificar el estado", error);
        }
    }

    @Override
    public Sesion iniciarSesion(String usuario, char[] contrasena)
            throws RemoteException, ValidacionException, AutenticacionException,
            ServicioNoDisponibleException {
        try {
            return autenticacion.iniciar(usuario, contrasena);
        } catch (RuntimeException error) {
            throw falloInesperado("iniciar sesión", error);
        }
    }

    @Override
    public void cerrarSesion(String token) throws RemoteException {
        try {
            sesiones.cerrar(token);
        } catch (RuntimeException error) {
            throw falloInesperado("cerrar sesión", error);
        }
    }

    @Override
    public List<Noticia> listarNoticias()
            throws RemoteException, ServicioNoDisponibleException {
        try {
            return noticias.listar();
        } catch (RuntimeException error) {
            throw falloInesperado("listar noticias", error);
        }
    }

    @Override
    public List<Noticia> buscarPorPalabraClave(String texto)
            throws RemoteException, ValidacionException, ServicioNoDisponibleException {
        try {
            return noticias.buscar(texto);
        } catch (RuntimeException error) {
            throw falloInesperado("buscar noticias", error);
        }
    }

    @Override
    public List<Noticia> buscarPorCategoria(Categoria categoria)
            throws RemoteException, ValidacionException, ServicioNoDisponibleException {
        try {
            return noticias.categoria(categoria);
        } catch (RuntimeException error) {
            throw falloInesperado("filtrar noticias", error);
        }
    }

    @Override
    public Noticia obtenerNoticia(long noticiaId)
            throws RemoteException, ValidacionException,
            NoticiaNoEncontradaException, ServicioNoDisponibleException {
        try {
            return noticias.obtener(noticiaId);
        } catch (RuntimeException error) {
            throw falloInesperado("consultar una noticia", error);
        }
    }

    @Override
    public Noticia publicarNoticia(String token, NuevaNoticia noticia)
            throws RemoteException, AutenticacionException, AutorizacionException,
            ValidacionException, ServicioNoDisponibleException {
        try {
            return noticias.publicar(token, noticia);
        } catch (RuntimeException error) {
            throw falloInesperado("publicar una noticia", error);
        }
    }

    @Override
    public Noticia editarNoticia(String token, long noticiaId,
                                 ActualizacionNoticia actualizacion,
                                 int versionEsperada)
            throws RemoteException, AutenticacionException, AutorizacionException,
            ValidacionException, NoticiaNoEncontradaException,
            ConflictoEdicionException, ServicioNoDisponibleException {
        try {
            return noticias.editar(
                    token, noticiaId, actualizacion, versionEsperada);
        } catch (RuntimeException error) {
            throw falloInesperado("editar una noticia", error);
        }
    }

    @Override
    public void eliminarNoticia(String token, long noticiaId)
            throws RemoteException, AutenticacionException, AutorizacionException,
            ValidacionException, NoticiaNoEncontradaException,
            ServicioNoDisponibleException {
        try {
            noticias.eliminar(token, noticiaId);
        } catch (RuntimeException error) {
            throw falloInesperado("eliminar una noticia", error);
        }
    }

    private RemoteException falloInesperado(String operacion, RuntimeException error) {
        LOGGER.error("Fallo inesperado al {}", operacion, error);
        return new RemoteException(
                "El servidor no pudo completar la operación solicitada.");
    }
}
