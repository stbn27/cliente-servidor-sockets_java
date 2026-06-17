package cliente;

import cliente.config.ConfiguracionCliente;
import cliente.rpc.ClienteRpcSalud;
import cliente.util.DecoradorConsola;
import cliente.util.LectorConsola;

/**
 * Cliente 2:
 * consume IMC e hipertensión.
 */
public class MainClienteDos {

    public static void main(String[] args) {
        LectorConsola lector = new LectorConsola();

        try {
            String host = args.length > 0 ? args[0] : ConfiguracionCliente.HOST_PREDETERMINADO;
            String urlServidor = ConfiguracionCliente.construirUrl(host);

            DecoradorConsola.titulo("Cliente");
            DecoradorConsola.info("Servidor destino: " + urlServidor);

            ClienteRpcSalud cliente = new ClienteRpcSalud(urlServidor);

            DecoradorConsola.seccion("Servicio de IMC");
            int peso = lector.leerEntero("Peso en kg: ");
            double estatura = lector.leerDouble("Estatura en metros: ");
            String respuestaImc = cliente.diagnosticarImc(peso, estatura);
            DecoradorConsola.exito(respuestaImc);

            DecoradorConsola.seccion("Servicio de hipertensión");
            int sistolica = lector.leerEntero("Presión sistólica: ");
            int diastolica = lector.leerEntero("Presión diastólica: ");
            String respuestaPresion = cliente.diagnosticarHipertension(sistolica, diastolica);
            DecoradorConsola.exito(respuestaPresion);

        } catch (Exception e) {
            DecoradorConsola.error("Error al consumir el servicio remoto: " + e.getMessage());
        } finally {
            lector.cerrar();
        }
    }
}