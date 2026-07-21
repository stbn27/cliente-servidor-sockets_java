package common.model;

import java.io.Serializable;

public final class Autor implements Serializable {

    private static final long serialVersionUID = 1L;

    private final long id;
    private final String usuario;
    private final String nombre;
    private final Rol rol;
    private final boolean activo;

    public Autor(long id, String usuario, String nombre, Rol rol, boolean activo) {
        this.id = id;
        this.usuario = usuario;
        this.nombre = nombre;
        this.rol = rol;
        this.activo = activo;
    }

    public long getId() {
        return id;
    }

    public String getUsuario() {
        return usuario;
    }

    public String getNombre() {
        return nombre;
    }

    public Rol getRol() {
        return rol;
    }

    public boolean isActivo() {
        return activo;
    }

}
