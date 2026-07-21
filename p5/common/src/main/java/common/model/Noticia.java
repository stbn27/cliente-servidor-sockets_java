package common.model;

import java.io.Serializable;
import java.time.LocalDateTime;

public record Noticia(long id, String titulo, String contenido, Categoria categoria, long autorId, String autorNombre,
                      LocalDateTime fechaCreacion, LocalDateTime fechaModificacion,
                      int version) implements Serializable {

    private static final long serialVersionUID = 1L;

}
