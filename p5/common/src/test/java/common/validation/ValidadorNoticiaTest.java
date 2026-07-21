package common.validation;

import common.exception.ValidacionException;
import common.model.ActualizacionNoticia;
import common.model.Categoria;
import common.model.NuevaNoticia;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ValidadorNoticiaTest {

    @Test
    void rechazaTituloVacio() {
        NuevaNoticia noticia = new NuevaNoticia("  ", "Contenido", Categoria.GENERAL);

        assertThrows(ValidacionException.class,
                () -> ValidadorNoticia.validarYNormalizar(noticia));
    }

    @Test
    void rechazaContenidoVacio() {
        NuevaNoticia noticia = new NuevaNoticia("Título", "\n\t ", Categoria.GENERAL);

        assertThrows(ValidacionException.class,
                () -> ValidadorNoticia.validarYNormalizar(noticia));
    }

    @Test
    void rechazaCategoriaNula() {
        NuevaNoticia noticia = new NuevaNoticia("Título", "Contenido", null);

        assertThrows(ValidacionException.class,
                () -> ValidadorNoticia.validarYNormalizar(noticia));
    }

    @Test
    void recortaEspaciosExteriores() throws ValidacionException {
        NuevaNoticia normalizada = ValidadorNoticia.validarYNormalizar(
                new NuevaNoticia("  Título  ", "\n Contenido \t", Categoria.CIENCIA));

        assertEquals("Título", normalizada.getTitulo());
        assertEquals("Contenido", normalizada.getContenido());
    }

    @Test
    void aceptaLosLimitesExactos() throws ValidacionException {
        NuevaNoticia normalizada = ValidadorNoticia.validarYNormalizar(
                new NuevaNoticia("a".repeat(ValidadorNoticia.TITULO_MAXIMO),
                        "b".repeat(ValidadorNoticia.CONTENIDO_MAXIMO), Categoria.CULTURA));

        assertEquals(ValidadorNoticia.TITULO_MAXIMO, normalizada.getTitulo().length());
        assertEquals(ValidadorNoticia.CONTENIDO_MAXIMO, normalizada.getContenido().length());
    }

    @Test
    void rechazaLongitudesMayoresAlLimite() {
        NuevaNoticia tituloLargo = new NuevaNoticia(
                "a".repeat(ValidadorNoticia.TITULO_MAXIMO + 1), "Contenido", Categoria.GENERAL);
        ActualizacionNoticia contenidoLargo = new ActualizacionNoticia(
                "Título", "b".repeat(ValidadorNoticia.CONTENIDO_MAXIMO + 1), Categoria.GENERAL);

        assertThrows(ValidacionException.class,
                () -> ValidadorNoticia.validarYNormalizar(tituloLargo));
        assertThrows(ValidacionException.class,
                () -> ValidadorNoticia.validarYNormalizar(contenidoLargo));
    }

    @Test
    void validaBusquedaIdentificadorYVersion() throws ValidacionException {
        assertEquals("rmi", ValidadorNoticia.validarYNormalizarBusqueda("  rmi "));
        assertThrows(ValidacionException.class,
                () -> ValidadorNoticia.validarYNormalizarBusqueda(" "));
        assertThrows(ValidacionException.class,
                () -> ValidadorNoticia.validarIdNoticia(0));
        assertThrows(ValidacionException.class,
                () -> ValidadorNoticia.validarVersion(-1));
        assertThrows(ValidacionException.class,
                () -> ValidadorNoticia.validarVersion(0));

        ValidadorNoticia.validarIdNoticia(1);
        ValidadorNoticia.validarVersion(1);
    }
}
