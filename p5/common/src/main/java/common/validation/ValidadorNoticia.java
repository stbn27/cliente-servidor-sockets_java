package common.validation;

import common.exception.ValidacionException;
import common.model.ActualizacionNoticia;
import common.model.NuevaNoticia;

/**
 * Reglas compartidas de entrada; el servidor siempre debe volver a aplicarlas.
 */
public final class ValidadorNoticia {

    public static final int TITULO_MAXIMO = 200;
    public static final int CONTENIDO_MAXIMO = 20_000;
    public static final int BUSQUEDA_MAXIMA = 100;

    private ValidadorNoticia() {
    }

    public static NuevaNoticia validarYNormalizar(NuevaNoticia noticia)
            throws ValidacionException {
        if (noticia == null) {
            throw new ValidacionException("La noticia es obligatoria.");
        }
        String titulo = normalizarTextoObligatorio(
                noticia.titulo(), "El título", TITULO_MAXIMO);
        String contenido = normalizarTextoObligatorio(
                noticia.contenido(), "El contenido", CONTENIDO_MAXIMO);
        if (noticia.categoria() == null) {
            throw new ValidacionException("La categoría es obligatoria.");
        }
        return new NuevaNoticia(titulo, contenido, noticia.categoria());
    }

    public static ActualizacionNoticia validarYNormalizar(ActualizacionNoticia actualizacion)
            throws ValidacionException {
        if (actualizacion == null) {
            throw new ValidacionException("La actualización es obligatoria.");
        }
        String titulo = normalizarTextoObligatorio(
                actualizacion.titulo(), "El título", TITULO_MAXIMO);
        String contenido = normalizarTextoObligatorio(
                actualizacion.contenido(), "El contenido", CONTENIDO_MAXIMO);
        if (actualizacion.categoria() == null) {
            throw new ValidacionException("La categoría es obligatoria.");
        }
        return new ActualizacionNoticia(titulo, contenido, actualizacion.categoria());
    }

    public static String validarYNormalizarBusqueda(String texto) throws ValidacionException {
        return normalizarTextoObligatorio(texto, "El texto de búsqueda", BUSQUEDA_MAXIMA);
    }

    public static void validarIdNoticia(long noticiaId) throws ValidacionException {
        if (noticiaId <= 0) {
            throw new ValidacionException("El identificador de la noticia debe ser positivo.");
        }
    }

    public static void validarVersion(int version) throws ValidacionException {
        if (version <= 0) {
            throw new ValidacionException("La versión debe ser positiva.");
        }
    }

    private static String normalizarTextoObligatorio(String texto, String campo, int maximo)
            throws ValidacionException {
        if (texto == null) {
            throw new ValidacionException(campo + " es obligatorio.");
        }
        String normalizado = texto.strip();
        if (normalizado.isEmpty()) {
            throw new ValidacionException(campo + " no puede estar vacío.");
        }
        if (normalizado.length() > maximo) {
            throw new ValidacionException(campo + " no puede superar " + maximo + " caracteres.");
        }
        return normalizado;
    }
}
