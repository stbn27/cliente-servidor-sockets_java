package servidor.rpc;

import java.util.Hashtable;
import servidor.config.ConfiguracionReplica;
import servidor.excepciones.CodigosError;

/**
 * Handler de diagnostico de la replica.
 */
public final class ManejadorSistemaRpc {

    private final ConfiguracionReplica configuracion;

    public ManejadorSistemaRpc(ConfiguracionReplica configuracion) {
        this.configuracion = configuracion;
    }

    public Hashtable<String, Object> ping() {
        Hashtable<String, Object> respuesta = new Hashtable<>();

        respuesta.put("exito", Boolean.TRUE);
        respuesta.put("codigo", CodigosError.OK);
        respuesta.put("mensaje", "Servidor replica disponible.");
        respuesta.put("detalleTecnico", "");
        respuesta.put("modulo", "SISTEMA_REPLICA");

        Hashtable<String, Object> datos = new Hashtable<>();

        datos.put("hostLocal", configuracion.getHostLocal());
        datos.put("puertoReplica", configuracion.getPuertoReplica());
        datos.put("categoriaReplicada", "inventario clinico");
        respuesta.put("datos", datos);

        return respuesta;
    }
}
