package servidor.repositorio;

import compartido.modelo.Evento;
import java.util.ArrayList;
import java.util.List;

public class AgendaRepositorio {

    private final List<Evento> eventos = new ArrayList<>();

    public synchronized void agregarEvento(String titulo, String fecha, String hora) {
        eventos.add(new Evento(titulo, fecha, hora));
    }

    public synchronized List<Evento> obtenerEventos() {
        return new ArrayList<>(eventos);
    }
}
