package server.repository;

import common.model.Rol;

public record AutorEntity(long id, String usuario, String passwordHash,
                          String nombre, Rol rol, boolean activo) {
}
