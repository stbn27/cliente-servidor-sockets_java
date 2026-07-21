package common.model;

import java.io.Serializable;
import java.time.LocalDateTime;

public final class Noticia implements Serializable {

    private static final long serialVersionUID = 1L;

    private final long id;
    private final String titulo;
    private final String contenido;
    private final Categoria categoria;
    private final long autorId;
    private final String autorNombre;
    private final LocalDateTime fechaCreacion;
    private final LocalDateTime fechaModificacion;
    private final int version;

    public Noticia(long id, String titulo, String contenido, Categoria categoria, long autorId, String autorNombre, LocalDateTime fechaCreacion, LocalDateTime fechaModificacion, int version) {
        this.id = id;
        this.titulo = titulo;
        this.contenido = contenido;
        this.categoria = categoria;
        this.autorId = autorId;
        this.autorNombre = autorNombre;
        this.fechaCreacion = fechaCreacion;
        this.fechaModificacion = fechaModificacion;
        this.version = version;
    }

    public long getId() {
        return id;
    }

    public String getTitulo() {
        return titulo;
    }

    public String getContenido() {
        return contenido;
    }

    public Categoria getCategoria() {
        return categoria;
    }

    public long getAutorId() {
        return autorId;
    }

    public String getAutorNombre() {
        return autorNombre;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public LocalDateTime getFechaModificacion() {
        return fechaModificacion;
    }

    public int getVersion() {
        return version;
    }

}
