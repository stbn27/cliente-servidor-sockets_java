package servidor.servicio;

import compartido.modelo.Evento;
import compartido.remoto.AgendaRemota;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;
import servidor.repositorio.AgendaRepositorio;

public class AgendaRemotaImpl extends UnicastRemoteObject implements AgendaRemota {

    private final AgendaRepositorio repositorio;

    public AgendaRemotaImpl(AgendaRepositorio repositorio) throws RemoteException {
        super();
        this.repositorio = repositorio;
    }

    @Override
    public List<Evento> consultarEventos() throws RemoteException {
        return repositorio.obtenerEventos();
    }

    @Override
    public void agregarEvento(String titulo, String fecha, String hora) throws RemoteException {
        repositorio.agregarEvento(titulo, fecha, hora);
    }
}
