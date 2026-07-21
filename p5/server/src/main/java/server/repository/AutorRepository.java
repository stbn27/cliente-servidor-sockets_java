package server.repository;

import common.model.Rol;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

public final class AutorRepository {
    public Optional<AutorEntity> buscarPorUsuario(Connection connection, String usuario) throws SQLException {
        String sql = "SELECT id, usuario, password_hash, nombre, rol, activo FROM autores WHERE usuario = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, usuario);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return Optional.empty();
                return Optional.of(new AutorEntity(rs.getLong("id"), rs.getString("usuario"),
                        rs.getString("password_hash"), rs.getString("nombre"),
                        Rol.valueOf(rs.getString("rol")), rs.getBoolean("activo")));
            }
        }
    }

    public long contar(Connection connection) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement("SELECT COUNT(*) FROM autores"); ResultSet rs = ps.executeQuery()) {
            rs.next(); return rs.getLong(1);
        }
    }
}
