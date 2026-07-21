package common.model;

import java.io.Serializable;

public record ActualizacionNoticia(String titulo, String contenido, Categoria categoria) implements Serializable {

    private static final long serialVersionUID = 1L;

}
