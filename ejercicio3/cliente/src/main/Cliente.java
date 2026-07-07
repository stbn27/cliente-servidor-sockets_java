package main;

import cliente.ui.MenuAgenda;
import compartido.remoto.AgendaRemota;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class Cliente {

    public static void main(String[] args) {
        try {
            // conexion al servidor
            Registry registro = LocateRegistry.getRegistry("localhost", 1099);
            AgendaRemota servicio = (AgendaRemota) registro.lookup("AgendaService");
            MenuAgenda menu = new MenuAgenda(servicio);
            menu.iniciar();
        } catch (Exception e) {
            System.out.println("Error al conectar con el servidor: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
