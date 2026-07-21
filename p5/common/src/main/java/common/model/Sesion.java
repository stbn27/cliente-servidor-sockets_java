package common.model;

import java.io.Serializable;
import java.time.Instant;

public final class Sesion implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String token;
    private final long autorId;
    private final String usuario;
    private final String autorNombre;
    private final Rol rol;
    private final Instant expiraEn;

    public Sesion(String token, long autorId, String usuario, String autorNombre,
                  Rol rol, Instant expiraEn) {
        this.token = token;
        this.autorId = autorId;
        this.usuario = usuario;
        this.autorNombre = autorNombre;
        this.rol = rol;
        this.expiraEn = expiraEn;
    }

    public String getToken() {
        return token;
    }

    public long getAutorId() {
        return autorId;
    }

    public String getUsuario() {
        return usuario;
    }

    public String getAutorNombre() {
        return autorNombre;
    }

    public Rol getRol() {
        return rol;
    }

    public Instant getExpiraEn() {
        return expiraEn;
    }
}
