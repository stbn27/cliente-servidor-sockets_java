package servidor.modelos;

import java.util.Hashtable;

/**
 * Resume metricas basicas de un usuario de redes sociales.
 */
public record EstadisticasUsuario(String usuario,
                                  int seguidores,
                                  int seguidos,
                                  int publicaciones,
                                  int interacciones,
                                  String pais) {

    public EstadisticasUsuario conPublicacionesIncrementadas() {
        return new EstadisticasUsuario(usuario, seguidores, seguidos, publicaciones + 1, interacciones, pais);
    }

    /**
     * Convierte la instancia a un mapa serializable por XML-RPC.
     *
     * @return datos estructurados del usuario.
     */
    public Hashtable<String, Object> toHashtable() {

        Hashtable<String, Object> tabla = new Hashtable<>();

        tabla.put("usuario", usuario);
        tabla.put("seguidores", seguidores);
        tabla.put("seguidos", seguidos);
        tabla.put("publicaciones", publicaciones);
        tabla.put("interacciones", interacciones);
        tabla.put("pais", pais);

        return tabla;
    }
}
