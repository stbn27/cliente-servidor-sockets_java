package server.database;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public final class DatabaseManager implements AutoCloseable {
    private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseManager.class);
    private final String jdbcUrl;

    public DatabaseManager(Path databasePath) throws SQLException {
        this(databasePath, false);
    }

    private DatabaseManager(Path databasePath, boolean memory) throws SQLException {
        if (memory) {
            jdbcUrl = "jdbc:h2:mem:" + databasePath + ";DB_CLOSE_DELAY=-1";
        } else {
            Path absolute = databasePath.toAbsolutePath().normalize();
            try { if (absolute.getParent() != null) Files.createDirectories(absolute.getParent()); }
            catch (Exception e) { throw new SQLException("No se pudo crear el directorio de base de datos", e); }
            jdbcUrl = "jdbc:h2:file:" + absolute + ";DB_CLOSE_ON_EXIT=FALSE";
        }
        try (Connection ignored = getConnection()) { LOGGER.info("Base H2 disponible"); }
    }

    public static DatabaseManager inMemory(String name) throws SQLException {
        return new DatabaseManager(Path.of(name.replaceAll("[^A-Za-z0-9_-]", "_")), true);
    }

    public Connection getConnection() throws SQLException { return DriverManager.getConnection(jdbcUrl, "sa", ""); }

    public boolean isAvailable() {
        try (Connection connection = getConnection()) { return connection.isValid(2); }
        catch (SQLException e) { return false; }
    }

    @Override public void close() {
        try (Connection connection = getConnection(); Statement statement = connection.createStatement()) {
            statement.execute("SHUTDOWN");
        } catch (SQLException e) { LOGGER.warn("No fue posible cerrar H2 limpiamente: {}", e.getMessage()); }
    }
}
