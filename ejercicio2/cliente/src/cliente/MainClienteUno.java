package cliente;

import cliente.config.ConfiguracionCliente;
import cliente.rpc.ClienteRpcSalud;
import cliente.util.DecoradorConsola;
import cliente.util.LectorConsola;

/**
 * Cliente 1:
 * consume hipertensión y glucosa en ayuno.
 */
public class MainClienteUno {

    public static void main(String[] args) {

        LectorConsola lector = new LectorConsola();

        try {

            String host = ConfiguracionCliente.HOST_PREDETERMINADO;
            String urlServidor = ConfiguracionCliente.construirUrl(host);

            DecoradorConsola.titulo("Cliente");
            DecoradorConsola.info("Servidor destino: " + urlServidor);

            ClienteRpcSalud cliente = new ClienteRpcSalud(urlServidor);

            // Servicio 1:
            DecoradorConsola.seccion("Servicio de hipertensión");
            int sistolica = lector.leerEntero("Presión sistólica: ");
            int diastolica = lector.leerEntero("Presión diastólica: ");
            String respuestaPresion = cliente.diagnosticarHipertension(sistolica, diastolica);
            DecoradorConsola.exito(respuestaPresion);

            // Servicio 2:
            DecoradorConsola.seccion("Servicio de glucosa en ayuno");
            int glucosa = lector.leerEntero("Glucosa en ayuno (mg/dL): ");
            String respuestaGlucosa = cliente.diagnosticarGlucosaAyuno(glucosa);
            DecoradorConsola.exito(respuestaGlucosa);

        } catch (Exception e) {
            DecoradorConsola.error("Error al consumir el servicio remoto: " + e.getMessage());
        } finally {
            lector.cerrar();
        }
    }
}