package common.model;

import java.io.Serializable;

public record Autor(long id, String usuario, String nombre, Rol rol, boolean activo) implements Serializable {

    private static final long serialVersionUID = 1L;

}
