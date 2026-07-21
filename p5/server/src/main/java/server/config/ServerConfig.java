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
        // Válida la dirección
        if (rmiHost == null || rmiHost.isBlank()) throw new IllegalArgumentException("rmi.host es obligatorio");
        // Válida el puerto 1 al 65535
        if (registryPort < 1 || registryPort > 65535) throw new IllegalArgumentException("rmi.port inválido");
        if (objectPort < 0 || objectPort > 65535) throw new IllegalArgumentException("rmi.object.port inválido");
        // Válida el nombre del servicio
        if (serviceName == null || serviceName.isBlank()) throw new IllegalArgumentException("rmi.service es obligatorio");
        // Válida la ruta de la base de datos
        if (databasePath == null) throw new IllegalArgumentException("database.path es obligatorio");
        // Válida el tiempo de sesión que sea positivo
        if (sessionTimeout == null || sessionTimeout.isZero() || sessionTimeout.isNegative()) throw new IllegalArgumentException("session.timeout.minutes debe ser positivo");
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

    /**
     * Convierte el valor asociado a {@code key} en el mapa a un número entero.
     *
     * @param values mapa de configuración donde buscar el valor
     * @param key    clave cuyo valor textual se desea convertir a {@code int}
     * @return el valor numérico convertido desde texto
     * @throws IllegalArgumentException si el valor es {@code null}, no es un
     *                                  número válido o no se puede parsear
     */
    private static int integer(Map<String, String> values, String key) {
        try {
            return Integer.parseInt(values.get(key)); // Intenta convertir el texto a int
        } catch (RuntimeException e) {
            // Captura cualquier error de conversión y lanza una excepción más descriptiva
            throw new IllegalArgumentException("Valor inválido para " + key);
        }
    }

    /**
     * Lee una variable de entorno del sistema operativo y, si existe y no está
     * vacía, la inserta en el mapa de configuración reemplazando el valor previo.
     * <p>
     * Si la variable de entorno no está definida o contiene solo espacios en
     * blanco, el mapa no se modifica y se conserva el valor original.
     *
     * @param values mapa donde se almacenará el valor (si existe)
     * @param env    nombre exacto de la variable de entorno del SO
     * @param key    clave interna del mapa bajo la que se guardará el valor
     */
    private static void putEnv(Map<String, String> values, String env, String key) {
        // Obtiene el valor de la variable de entorno del sistema operativo
        String value = System.getenv(env);
        // Solo lo guarda si no es null y no está vacío o en blanco
        if (value != null && !value.isBlank()) {
            values.put(key, value); // Sobrescribe el valor previo del mapa
        }
    }
}
