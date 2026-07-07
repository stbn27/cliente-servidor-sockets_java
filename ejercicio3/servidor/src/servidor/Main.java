package servidor;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import servidor.repositorio.AgendaRepositorio;
import servidor.servicio.AgendaRemotaImpl;

public class Main {

    public static void main(String[] args) {
        try {
            // inicializacion del servidor
            AgendaRepositorio repositorio = new AgendaRepositorio();
            AgendaRemotaImpl servicio = new AgendaRemotaImpl(repositorio);
            Registry registro = LocateRegistry.createRegistry(1099);
            registro.rebind("AgendaService", servicio);
            System.out.println("Servidor RMI listo en localhost:1099");

        } catch (Exception e) {
            System.out.println("Error al iniciar el servidor: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
