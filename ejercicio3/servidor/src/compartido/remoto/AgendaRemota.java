package compartido.remoto;

import compartido.modelo.Evento;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface AgendaRemota extends Remote {

    List<Evento> consultarEventos() throws RemoteException;

    void agregarEvento(String titulo, String fecha, String hora) throws RemoteException;
}
