package common.model;

import java.io.Serializable;

public final class NuevaNoticia implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String titulo;
    private final String contenido;
    private final Categoria categoria;

    public NuevaNoticia(String titulo, String contenido, Categoria categoria) {
        this.titulo = titulo;
        this.contenido = contenido;
        this.categoria = categoria;
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
}
