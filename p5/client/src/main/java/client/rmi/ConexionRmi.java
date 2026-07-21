package client.rmi;

import common.dto.EstadoServidor;
import common.remote.TableroNoticiasRemote;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public final class ConexionRmi {

    private final String host;
    private final int puerto;
    private final String servicioNombre;
    private final TableroNoticiasRemote servicio;

    private ConexionRmi(String host, int puerto, String servicioNombre,
                        TableroNoticiasRemote servicio) {
        this.host = host;
        this.puerto = puerto;
        this.servicioNombre = servicioNombre;
        this.servicio = servicio;
    }

    public static ConexionRmi conectar(String host, int puerto, String servicioNombre)
            throws RemoteException, NotBoundException {
        Registry registry = LocateRegistry.getRegistry(host, puerto);
        TableroNoticiasRemote remoto =
                (TableroNoticiasRemote) registry.lookup(servicioNombre);
        EstadoServidor estado = remoto.verificarEstado();
        if (estado == null || !estado.disponible()
                || !estado.baseDatosDisponible()) {
            throw new RemoteException("El servidor no informó un estado disponible.");
        }
        return new ConexionRmi(host, puerto, servicioNombre, remoto);
    }

    public String getHost() {
        return host;
    }

    public int getPuerto() {
        return puerto;
    }

    public String getServicioNombre() {
        return servicioNombre;
    }

    public TableroNoticiasRemote getServicio() {
        return servicio;
    }

    public String descripcion() {
        return host + ":" + puerto + "/" + servicioNombre;
    }
}
