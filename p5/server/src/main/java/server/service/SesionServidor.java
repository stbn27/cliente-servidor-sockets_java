package server.service;

import common.model.Rol;
import common.model.Sesion;

import java.time.Instant;

record SesionServidor(String token, long autorId, String usuario, String autorNombre, Rol rol, Instant expiraEn) {
    Sesion publica() {
        return new Sesion(token, autorId, usuario, autorNombre, rol, expiraEn);
    }
}
