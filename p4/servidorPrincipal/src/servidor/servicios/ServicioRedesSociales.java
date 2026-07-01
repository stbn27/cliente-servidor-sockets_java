package servidor.servicios;

import servidor.excepciones.ErrorAplicacion;
import servidor.modelos.EstadisticasUsuario;
import servidor.modelos.Publicacion;
import servidor.modelos.TendenciaPais;
import servidor.repositorios.RepositorioRedesSociales;
import servidor.validacion.ValidadorEntrada;

/**
 * Orquesta reglas de negocio de redes sociales.
 */
public final class ServicioRedesSociales {

    private static final String MODULO = "REDES_SOCIALES";

    private final RepositorioRedesSociales repositorio;

    public ServicioRedesSociales(RepositorioRedesSociales repositorio) {
        this.repositorio = repositorio;
    }

    /**
     * Recupera tendencias de un pais.
     *
     * @param pais nombre del pais.
     * @return tendencias encontradas.
     * @throws ErrorAplicacion si la entrada es invalida o el recurso no existe.
     */
    public TendenciaPais consultarTendenciasPorPais(String pais) throws ErrorAplicacion {
        ValidadorEntrada.validarTextoObligatorio(pais, "pais", MODULO);
        return repositorio.buscarTendenciasPorPais(pais.trim());
    }

    /**
     * Busca estadisticas de un usuario.
     *
     * @param usuario alias del usuario.
     * @return estadisticas del usuario.
     * @throws ErrorAplicacion si hay datos invalidos o el usuario no existe.
     */
    public EstadisticasUsuario consultarEstadisticasUsuario(String usuario) throws ErrorAplicacion {
        ValidadorEntrada.validarTextoObligatorio(usuario, "usuario", MODULO);
        return repositorio.buscarEstadisticasUsuario(usuario.trim());
    }

    /**
     * Publica un mensaje nuevo.
     *
     * @param usuario usuario autor.
     * @param pais pais relacionado con la publicacion.
     * @param mensaje mensaje a guardar.
     * @return publicacion persistida.
     * @throws ErrorAplicacion si la entrada no es valida o falla la persistencia.
     */
    public Publicacion publicarNuevaPublicacion(String usuario, String pais, String mensaje)
            throws ErrorAplicacion {

        ValidadorEntrada.validarTextoObligatorio(usuario, "usuario", MODULO);
        ValidadorEntrada.validarTextoObligatorio(pais, "pais", MODULO);
        ValidadorEntrada.validarTextoObligatorio(mensaje, "mensaje", MODULO);

        return repositorio.guardarPublicacion(usuario.trim(), pais.trim(), mensaje.trim());
    }
}
