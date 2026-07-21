package server.repository;

import common.model.ActualizacionNoticia;
import common.model.Categoria;
import common.model.Noticia;
import common.model.NuevaNoticia;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class NoticiaRepository {
    private static final String SELECT = "SELECT n.id,n.titulo,n.contenido,n.categoria,n.autor_id,a.nombre autor_nombre,n.fecha_creacion,n.fecha_modificacion,n.version FROM noticias n JOIN autores a ON a.id=n.autor_id ";
    private static final String ORDER = " ORDER BY n.fecha_modificacion DESC,n.id DESC";

    public List<Noticia> listar(Connection c) throws SQLException {
        return query(c, SELECT + ORDER);
    }

    public List<Noticia> buscar(Connection c, String text) throws SQLException {
        String escaped = text.toLowerCase().replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
        return query(c, SELECT + "WHERE LOWER(n.titulo) LIKE ? ESCAPE '\\' OR LOWER(n.contenido) LIKE ? ESCAPE '\\'" + ORDER, "%" + escaped + "%", "%" + escaped + "%");
    }

    public List<Noticia> porCategoria(Connection c, Categoria category) throws SQLException {
        return query(c, SELECT + "WHERE n.categoria=?" + ORDER, category.name());
    }

    public Optional<Noticia> obtener(Connection c, long id) throws SQLException {
        List<Noticia> result = query(c, SELECT + "WHERE n.id=?", id);
        return result.stream().findFirst();
    }

    public long insertar(Connection c, NuevaNoticia news, long authorId) throws SQLException {
        String sql = "INSERT INTO noticias(titulo,contenido,categoria,autor_id,version,fecha_creacion,fecha_modificacion) VALUES(?,?,?,?,1,?,?)";
        try (PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            LocalDateTime now = LocalDateTime.now();
            ps.setString(1, news.titulo());
            ps.setString(2, news.contenido());
            ps.setString(3, news.categoria().name());
            ps.setLong(4, authorId);
            ps.setObject(5, now);
            ps.setObject(6, now);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (!rs.next()) throw new SQLException("H2 no devolvió el identificador");
                return rs.getLong(1);
            }
        }
    }

    public int actualizar(Connection c, long id, long authorId, int expected, ActualizacionNoticia update) throws SQLException {
        String sql = "UPDATE noticias SET titulo=?,contenido=?,categoria=?,version=version+1,fecha_modificacion=? WHERE id=? AND autor_id=? AND version=?";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, update.titulo());
            ps.setString(2, update.contenido());
            ps.setString(3, update.categoria().name());
            ps.setObject(4, LocalDateTime.now());
            ps.setLong(5, id);
            ps.setLong(6, authorId);
            ps.setInt(7, expected);
            return ps.executeUpdate();
        }
    }

    public int eliminar(Connection c, long id, long authorId) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement("DELETE FROM noticias WHERE id=? AND autor_id=?")) {
            ps.setLong(1, id);
            ps.setLong(2, authorId);
            return ps.executeUpdate();
        }
    }

    public Optional<EstadoNoticia> estado(Connection c, long id) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement("SELECT autor_id,version FROM noticias WHERE id=?")) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(new EstadoNoticia(rs.getLong(1), rs.getInt(2))) : Optional.empty();
            }
        }
    }

    public long contar(Connection c) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement("SELECT COUNT(*) FROM noticias"); ResultSet rs = ps.executeQuery()) {
            rs.next();
            return rs.getLong(1);
        }
    }

    private List<Noticia> query(Connection c, String sql, Object... params) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) ps.setObject(i + 1, params[i]);
            try (ResultSet rs = ps.executeQuery()) {
                List<Noticia> list = new ArrayList<>();
                while (rs.next()) list.add(map(rs));
                return List.copyOf(list);
            }
        }
    }

    private Noticia map(ResultSet rs) throws SQLException {
        return new Noticia(rs.getLong("id"), rs.getString("titulo"), rs.getString("contenido"), Categoria.valueOf(rs.getString("categoria")), rs.getLong("autor_id"), rs.getString("autor_nombre"), rs.getObject("fecha_creacion", LocalDateTime.class), rs.getObject("fecha_modificacion", LocalDateTime.class), rs.getInt("version"));
    }

    public record EstadoNoticia(long autorId, int version) {
    }
}
