package server.tools;

import at.favre.lib.crypto.bcrypt.BCrypt;
import common.model.Rol;
import server.config.ServerConfig;
import server.database.DatabaseInitializer;
import server.database.DatabaseManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;

public final class UsuarioAdminTool {
    private static final int BCRYPT_COST = 12;

    private UsuarioAdminTool() {
    }

    public static void main(String[] args) throws Exception {
        if (args.length == 0 || "ayuda".equalsIgnoreCase(args[0]) || "--help".equalsIgnoreCase(args[0])) {
            printUsage();
            return;
        }

        ServerConfig config = ServerConfig.load(extraerConfigArgs(args));
        try (DatabaseManager database = new DatabaseManager(config.databasePath())) {
            new DatabaseInitializer(database).initialize();
            String command = args[0].toLowerCase();
            switch (command) {
                case "listar" -> listar(database);
                case "crear" -> crear(database, args);
                case "activar" -> cambiarEstado(database, args, true);
                case "desactivar" -> cambiarEstado(database, args, false);
                default -> {
                    System.err.println("Comando no reconocido: " + args[0]);
                    printUsage();
                    System.exit(1);
                }
            }
        }
    }

    private static String[] extraerConfigArgs(String[] args) {
        return Arrays.stream(args)
                .filter(arg -> arg.startsWith("--"))
                .toArray(String[]::new);
    }

    private static void listar(DatabaseManager database) throws SQLException {
        String sql = "SELECT id, usuario, nombre, rol, activo FROM autores ORDER BY id";
        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rows = statement.executeQuery()) {
            System.out.printf("%-5s %-20s %-30s %-12s %-8s%n", "ID", "USUARIO", "NOMBRE", "ROL", "ACTIVO");
            while (rows.next()) {
                System.out.printf("%-5d %-20s %-30s %-12s %-8s%n",
                        rows.getLong("id"),
                        rows.getString("usuario"),
                        rows.getString("nombre"),
                        rows.getString("rol"),
                        rows.getBoolean("activo"));
            }
        }
    }

    private static void crear(DatabaseManager database, String[] args) throws SQLException {
        if (args.length < 4) {
            throw new IllegalArgumentException("Uso: crear <usuario> <contrasena> <nombre visible>");
        }
        String usuario = normalizar(args[1], "usuario", 50);
        char[] password = args[2].toCharArray();
        String nombre = normalizar(unir(args, 3), "nombre visible", 100);
        try {
            if (password.length < 6 || password.length > 72) {
                throw new IllegalArgumentException("La contrasena debe tener entre 6 y 72 caracteres");
            }
            String hash = BCrypt.withDefaults().hashToString(BCRYPT_COST, password);
            String sql = "INSERT INTO autores(usuario, password_hash, nombre, rol, activo) VALUES(?, ?, ?, ?, TRUE)";
            try (Connection connection = database.getConnection();
                 PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, usuario);
                statement.setString(2, hash);
                statement.setString(3, nombre);
                statement.setString(4, Rol.REDACTOR.name());
                statement.executeUpdate();
            }
            System.out.println("Usuario creado: " + usuario);
        } finally {
            Arrays.fill(password, '\0');
        }
    }

    private static void cambiarEstado(DatabaseManager database, String[] args, boolean activo) throws SQLException {
        if (args.length < 2) {
            throw new IllegalArgumentException("Uso: " + (activo ? "activar" : "desactivar") + " <usuario>");
        }
        String usuario = normalizar(args[1], "usuario", 50);
        String sql = "UPDATE autores SET activo=? WHERE usuario=?";
        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setBoolean(1, activo);
            statement.setString(2, usuario);
            int updated = statement.executeUpdate();
            if (updated == 0) {
                throw new IllegalArgumentException("No existe el usuario: " + usuario);
            }
        }
        System.out.println("Usuario " + usuario + " actualizado. activo=" + activo);
    }

    private static String normalizar(String value, String field, int maxLength) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("El campo " + field + " es obligatorio");
        }
        String normalized = value.trim();
        if (normalized.length() > maxLength) {
            throw new IllegalArgumentException("El campo " + field + " no puede superar " + maxLength + " caracteres");
        }
        return normalized;
    }

    private static String unir(String[] args, int start) {
        StringBuilder builder = new StringBuilder();
        for (int i = start; i < args.length; i++) {
            if (!args[i].startsWith("--")) {
                if (!builder.isEmpty()) {
                    builder.append(' ');
                }
                builder.append(args[i]);
            }
        }
        return builder.toString();
    }

    private static void printUsage() {
        System.out.println("""
                Administracion basica de usuarios redactores

                Uso:
                  java -cp tablero-noticias-server.jar server.tools.UsuarioAdminTool listar
                  java -cp tablero-noticias-server.jar server.tools.UsuarioAdminTool crear <usuario> <contrasena> <nombre visible>
                  java -cp tablero-noticias-server.jar server.tools.UsuarioAdminTool activar <usuario>
                  java -cp tablero-noticias-server.jar server.tools.UsuarioAdminTool desactivar <usuario>

                Configuracion opcional:
                  --database.path=./data/tablero-noticias
                """);
    }
}
