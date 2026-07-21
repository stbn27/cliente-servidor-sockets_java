package server.config;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Properties;

public record ServerConfig(String rmiHost, int registryPort, int objectPort,
                           String serviceName, Path databasePath, Duration sessionTimeout) {
    public ServerConfig {
        // Válida la dirección
        if (rmiHost == null || rmiHost.isBlank()) throw new IllegalArgumentException("rmi.host es obligatorio");
        // Válida el puerto 1 al 65535
        if (registryPort < 1 || registryPort > 65535) throw new IllegalArgumentException("rmi.port inválido");
        if (objectPort < 0 || objectPort > 65535) throw new IllegalArgumentException("rmi.object.port inválido");
        // Válida el nombre del servicio
        if (serviceName == null || serviceName.isBlank())
            throw new IllegalArgumentException("rmi.service es obligatorio");
        // Válida la ruta de la base de datos
        if (databasePath == null) throw new IllegalArgumentException("database.path es obligatorio");
        // Válida el tiempo de sesión que sea positivo
        if (sessionTimeout == null || sessionTimeout.isZero() || sessionTimeout.isNegative())
            throw new IllegalArgumentException("session.timeout.minutes debe ser positivo");
        // Convierte la ruta de la base de datos a una ruta completa y limpia
        databasePath = databasePath.toAbsolutePath().normalize();
    }

    /**
     * Carga la configuración exclusivamente desde el archivo
     * {@code server.properties} embebido en el classpath.
     *
     * @return instancia inmutable de {@link ServerConfig} ya validada
     * @throws IOException              si no se puede leer el archivo
     * @throws IllegalArgumentException si falta algún valor o es inválido
     */
    public static ServerConfig load(String[] args) throws IOException {
        Properties p = new Properties();

        // Cargamos el archivo de variables/credenciales
        try (InputStream in = ServerConfig.class.getResourceAsStream("/server.properties")) {
            if (in != null) p.load(in); // Solo carga si el archivo existe en el classpath
        }

        // Construye el record leyendo cada propiedad directamente
        return new ServerConfig(
                p.getProperty("rmi.host"),
                Integer.parseInt(p.getProperty("rmi.port")),
                Integer.parseInt(p.getProperty("rmi.object.port")),
                p.getProperty("rmi.service"),
                Path.of(p.getProperty("database.path")),
                Duration.ofMinutes(Integer.parseInt(p.getProperty("session.timeout.minutes")))
        );
    }
}
