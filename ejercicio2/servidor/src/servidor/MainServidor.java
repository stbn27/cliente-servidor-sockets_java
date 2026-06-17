package servidor;

import org.apache.xmlrpc.WebServer;
import servidor.config.ConfiguracionServidor;
import servidor.rpc.ServicioSaludRpc;
import servidor.util.DecoradorConsola;

/**
 * Punto de entrada del servidor.
 *
 * @author Esteban Nuñez
 * @version 1.0
 */
public class MainServidor {

    public static void main(String[] args) {
        try {
            DecoradorConsola.titulo("Servidor XML-RPC de salud");
            DecoradorConsola.seccion("Inicialización");

            WebServer servidor = new WebServer(ConfiguracionServidor.PUERTO);
            servidor.addHandler(ConfiguracionServidor.NOMBRE_HANDLER, new ServicioSaludRpc());
            servidor.start();

            DecoradorConsola.exito("Servidor iniciado correctamente.");
            DecoradorConsola.info("Puerto: " + ConfiguracionServidor.PUERTO);
            DecoradorConsola.info("Endpoint para clientes: /RPC2");
            DecoradorConsola.info("Handler registrado: " + ConfiguracionServidor.NOMBRE_HANDLER);
            DecoradorConsola.info("Métodos disponibles:");
            DecoradorConsola.info("  - salud.diagnosticarImc");
            DecoradorConsola.info("  - salud.diagnosticarHipertension");
            DecoradorConsola.info("  - salud.diagnosticarGlucosaAyuno");
            DecoradorConsola.info("Servidor en espera de solicitudes remotas...");
        } catch (Exception e) {
            DecoradorConsola.error("No fue posible iniciar el servidor: " + e.getMessage());
        }
    }
}