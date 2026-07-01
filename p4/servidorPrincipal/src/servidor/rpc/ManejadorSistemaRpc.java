package servidor.rpc;

import java.util.Hashtable;
import servidor.config.ConfiguracionServidor;
import servidor.excepciones.CodigosError;

/**
 * Handler de diagnostico general del servidor principal.
 */
public final class ManejadorSistemaRpc {

    private final ConfiguracionServidor configuracion;

    public ManejadorSistemaRpc(ConfiguracionServidor configuracion) {
        this.configuracion = configuracion;
    }

    /**
     * Verifica que el servidor principal este vivo.
     *
     * @return respuesta simple para pruebas manuales.
     */
    public Hashtable<String, Object> ping() {
        Hashtable<String, Object> respuesta = new Hashtable<>();

        respuesta.put("exito", Boolean.TRUE);
        respuesta.put("codigo", CodigosError.OK);
        respuesta.put("mensaje", "Servidor principal disponible.");
        respuesta.put("detalleTecnico", "");
        respuesta.put("modulo", "SISTEMA");

        Hashtable<String, Object> datos = new Hashtable<>();

        datos.put("hostLocal", configuracion.getHostLocal());
        datos.put("puertoPrincipal", configuracion.getPuertoPrincipal());
        datos.put("hostReplica", configuracion.getHostReplica());
        datos.put("puertoReplica", configuracion.getPuertoReplica());
        datos.put("categoriaReplicada", "inventario clinico");

        respuesta.put("datos", datos);

        return respuesta;
    }
}
