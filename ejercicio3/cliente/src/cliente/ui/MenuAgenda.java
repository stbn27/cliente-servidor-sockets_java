package cliente.ui;

import compartido.modelo.Evento;
import compartido.remoto.AgendaRemota;
import java.util.List;
import java.util.Scanner;

public class MenuAgenda {

    private final AgendaRemota servicio;
    private final Scanner scanner;

    public MenuAgenda(AgendaRemota servicio) {
        this.servicio = servicio;
        this.scanner = new Scanner(System.in);
    }

    public void iniciar() {
        int opcion;

        do {
            mostrarMenu();
            opcion = leerEntero();

            switch (opcion) {
                case 1 -> verEventos();
                case 2 -> agregarEvento();
                case 3 -> System.out.println("Hasta luego.");
                default -> System.out.println("Opcion invalida.");
            }
        } while (opcion != 3);
    }

    private void mostrarMenu() {
        System.out.println();
        System.out.println("1. Ver eventos");
        System.out.println("2. Agregar evento");
        System.out.println("3. Salir");
        System.out.print("Selecciona una opcion: ");
    }

    private int leerEntero() {
        String texto = scanner.nextLine();

        try {
            return Integer.parseInt(texto);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private void verEventos() {
        try {
            List<Evento> eventos = servicio.consultarEventos();

            if (eventos.isEmpty()) {
                System.out.println("No hay eventos registrados.");
                return;
            }

            System.out.println("Eventos registrados:");
            for (int i = 0; i < eventos.size(); i++) {
                System.out.println((i + 1) + ". " + eventos.get(i));
            }
        } catch (Exception e) {
            System.out.println("No fue posible consultar los eventos: " + e.getMessage());
        }
    }

    private void agregarEvento() {
        try {
            System.out.print("Titulo: ");
            String titulo = scanner.nextLine().trim();
            System.out.print("Fecha: ");
            String fecha = scanner.nextLine().trim();
            System.out.print("Hora: ");
            String hora = scanner.nextLine().trim();

            servicio.agregarEvento(titulo, fecha, hora);
            System.out.println("Evento agregado.");
        } catch (Exception e) {
            System.out.println("No fue posible agregar el evento: " + e.getMessage());
        }
    }
}
