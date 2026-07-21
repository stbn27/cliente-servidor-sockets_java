package server.bootstrap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import server.config.ServerConfig;
import server.database.DatabaseInitializer;
import server.database.DatabaseManager;
import server.remote.TableroNoticiasRemoteImpl;
import server.repository.AutorRepository;
import server.repository.NoticiaRepository;
import server.service.AutenticacionService;
import server.service.NoticiaService;
import server.service.SesionService;

import java.rmi.Remote;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.ExportException;
import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.CountDownLatch;

public final class ServidorApplication {
    private static final Logger LOGGER = LoggerFactory.getLogger(ServidorApplication.class);

    private ServidorApplication() {
    }

    public static void main(String[] args) {
        try {
            start(ServerConfig.load(args), true);
        } catch (Exception error) {
            LOGGER.error("No fue posible iniciar el servidor", error);
            System.exit(1);
        }
    }

    public static RuntimeHandle start(ServerConfig config, boolean wait) throws Exception {
        System.setProperty("java.rmi.server.hostname", config.rmiHost());
        DatabaseManager database = new DatabaseManager(config.databasePath());
        Registry registry = null;
        boolean registryPropio = false;
        TableroNoticiasRemoteImpl implementacion = null;
        try {
            new DatabaseInitializer(database).initialize();
            AutorRepository autores = new AutorRepository();
            NoticiaRepository noticias = new NoticiaRepository();
            SesionService sesiones = new SesionService(config.sessionTimeout());
            AutenticacionService autenticacion =
                    new AutenticacionService(database, autores, sesiones);
            NoticiaService servicioNoticias =
                    new NoticiaService(database, noticias, sesiones);
            implementacion = new TableroNoticiasRemoteImpl(
                    database, autenticacion, servicioNoticias, sesiones);

            Remote stub = UnicastRemoteObject.exportObject(implementacion, config.objectPort());
            try {
                registry = LocateRegistry.createRegistry(config.registryPort());
                registryPropio = true;
                LOGGER.info("Registry RMI creado en puerto {}", config.registryPort());
            } catch (ExportException puertoOcupado) {
                registry = LocateRegistry.getRegistry("localhost", config.registryPort());
                registry.list();
                LOGGER.info("Registry RMI existente localizado en puerto {}", config.registryPort());
            }

            registry.rebind(config.serviceName(), stub);
            RuntimeHandle handle = new RuntimeHandle(
                    config, database, registry, implementacion, registryPropio);
            Runtime.getRuntime().addShutdownHook(
                    new Thread(handle::close, "cierre-servidor"));
            LOGGER.info("Servicio {} registrado; host={}, registry={}, objeto={}",
                    config.serviceName(), config.rmiHost(),
                    config.registryPort(), config.objectPort());
            if (wait) {
                new CountDownLatch(1).await();
            }
            return handle;
        } catch (Exception error) {
            if (implementacion != null) {
                try {
                    UnicastRemoteObject.unexportObject(implementacion, true);
                } catch (Exception cleanupError) {
                    LOGGER.debug("El objeto remoto no requería desexportación", cleanupError);
                }
            }
            if (registryPropio && registry != null) {
                try {
                    UnicastRemoteObject.unexportObject(registry, true);
                } catch (Exception cleanupError) {
                    LOGGER.debug("El Registry no requería desexportación", cleanupError);
                }
            }
            database.close();
            throw error;
        }
    }

    public static final class RuntimeHandle implements AutoCloseable {
        private final ServerConfig config;
        private final DatabaseManager database;
        private final Registry registry;
        private final Remote implementacion;
        private final boolean registryPropio;
        private volatile boolean cerrado;

        RuntimeHandle(ServerConfig config, DatabaseManager database, Registry registry,
                      Remote implementacion, boolean registryPropio) {
            this.config = config;
            this.database = database;
            this.registry = registry;
            this.implementacion = implementacion;
            this.registryPropio = registryPropio;
        }

        @Override
        public synchronized void close() {
            if (cerrado) {
                return;
            }
            cerrado = true;
            try {
                registry.unbind(config.serviceName());
            } catch (Exception error) {
                LOGGER.debug("El servicio ya no estaba registrado durante el cierre", error);
            }
            try {
                UnicastRemoteObject.unexportObject(implementacion, true);
            } catch (Exception error) {
                LOGGER.debug("El servicio remoto ya estaba desexportado", error);
            }
            if (registryPropio) {
                try {
                    UnicastRemoteObject.unexportObject(registry, true);
                } catch (Exception error) {
                    LOGGER.debug("El Registry propio ya estaba desexportado", error);
                }
            }
            database.close();
            LOGGER.info("Servidor detenido");
        }
    }
}
