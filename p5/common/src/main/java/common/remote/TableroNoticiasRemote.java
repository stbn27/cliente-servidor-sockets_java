package common.remote;

import common.dto.EstadoServidor;
import common.exception.*;
import common.model.*;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface TableroNoticiasRemote extends Remote {

    EstadoServidor verificarEstado()
            throws RemoteException;

    Sesion iniciarSesion(String usuario, char[] contrasena)
            throws RemoteException, ValidacionException, AutenticacionException,
            ServicioNoDisponibleException;

    void cerrarSesion(String token)
            throws RemoteException;

    List<Noticia> listarNoticias()
            throws RemoteException, ServicioNoDisponibleException;

    List<Noticia> buscarPorPalabraClave(String texto)
            throws RemoteException, ValidacionException, ServicioNoDisponibleException;

    List<Noticia> buscarPorCategoria(Categoria categoria)
            throws RemoteException, ValidacionException, ServicioNoDisponibleException;

    Noticia obtenerNoticia(long noticiaId)
            throws RemoteException, ValidacionException, NoticiaNoEncontradaException,
            ServicioNoDisponibleException;

    Noticia publicarNoticia(String token, NuevaNoticia noticia)
            throws RemoteException,
            AutenticacionException,
            AutorizacionException,
            ValidacionException,
            ServicioNoDisponibleException;

    Noticia editarNoticia(
            String token,
            long noticiaId,
            ActualizacionNoticia actualizacion,
            int versionEsperada
    ) throws RemoteException,
            AutenticacionException,
            AutorizacionException,
            ValidacionException,
            NoticiaNoEncontradaException,
            ConflictoEdicionException,
            ServicioNoDisponibleException;

    void eliminarNoticia(String token, long noticiaId)
            throws RemoteException,
            AutenticacionException,
            AutorizacionException,
            ValidacionException,
            NoticiaNoEncontradaException,
            ServicioNoDisponibleException;
}
