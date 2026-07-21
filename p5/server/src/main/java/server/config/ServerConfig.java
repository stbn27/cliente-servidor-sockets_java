package server.config;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public record ServerConfig(String rmiHost, int registryPort, int objectPort,
                           String serviceName, Path databasePath, Duration sessionTimeout) {
    public ServerConfig {
        if (rmiHost == null || rmiHost.isBlank()) throw new IllegalArgumentException("rmi.host es obligatorio");
        if (registryPort < 1 || registryPort > 65535) throw new IllegalArgumentException("rmi.port inválido");
        if (objectPort < 0 || objectPort > 65535) throw new IllegalArgumentException("rmi.object.port inválido");
        if (serviceName == null || serviceName.isBlank()) throw new IllegalArgumentException("rmi.service es obligatorio");
        if (databasePath == null) throw new IllegalArgumentException("database.path es obligatorio");
        if (sessionTimeout == null || sessionTimeout.isZero() || sessionTimeout.isNegative()) throw new IllegalArgumentException("session.timeout.minutes debe ser positivo");
        databasePath = databasePath.toAbsolutePath().normalize();
    }

    public static ServerConfig load(String[] args) throws IOException {
        Properties p = new Properties();
        try (InputStream in = ServerConfig.class.getResourceAsStream("/server.properties")) {
            if (in != null) p.load(in);
        }
        Map<String, String> values = new HashMap<>();
        p.forEach((k, v) -> values.put(k.toString(), v.toString()));
        putEnv(values, "RMI_HOST", "rmi.host"); putEnv(values, "RMI_PORT", "rmi.port");
        putEnv(values, "RMI_OBJECT_PORT", "rmi.object.port"); putEnv(values, "RMI_SERVICE", "rmi.service");
        putEnv(values, "DATABASE_PATH", "database.path"); putEnv(values, "SESSION_TIMEOUT_MINUTES", "session.timeout.minutes");
        for (String arg : args == null ? new String[0] : args) {
            if (arg.startsWith("--") && arg.contains("=")) {
                int equals = arg.indexOf('='); values.put(arg.substring(2, equals), arg.substring(equals + 1));
            }
        }
        return new ServerConfig(values.get("rmi.host"), integer(values, "rmi.port"), integer(values, "rmi.object.port"),
                values.get("rmi.service"), Path.of(values.get("database.path")),
                Duration.ofMinutes(integer(values, "session.timeout.minutes")));
    }

    private static int integer(Map<String, String> values, String key) {
        try { return Integer.parseInt(values.get(key)); }
        catch (RuntimeException e) { throw new IllegalArgumentException("Valor inválido para " + key); }
    }
    private static void putEnv(Map<String, String> values, String env, String key) {
        String value = System.getenv(env); if (value != null && !value.isBlank()) values.put(key, value);
    }
}
