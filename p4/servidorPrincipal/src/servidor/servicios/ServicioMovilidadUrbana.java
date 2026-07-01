package servidor.servicios;

import servidor.excepciones.ErrorAplicacion;
import servidor.modelos.EstacionEcoBici;
import servidor.modelos.IncidenteVial;
import servidor.modelos.TraficoZona;
import servidor.repositorios.RepositorioMovilidadUrbana;
import servidor.validacion.ValidadorEntrada;

/**
 * Servicio de negocio para la categoria movilidad urbana.
 */
public final class ServicioMovilidadUrbana {

    private static final String MODULO = "MOVILIDAD_URBANA";

    private final RepositorioMovilidadUrbana repositorio;

    public ServicioMovilidadUrbana(RepositorioMovilidadUrbana repositorio) {
        this.repositorio = repositorio;
    }

    public TraficoZona consultarTraficoPorZona(String zona) throws ErrorAplicacion {
        ValidadorEntrada.validarTextoObligatorio(zona, "zona", MODULO);
        return repositorio.buscarTraficoPorZona(zona.trim());
    }

    public EstacionEcoBici consultarEcoBicicletasPorEstacion(String estacion) throws ErrorAplicacion {
        ValidadorEntrada.validarTextoObligatorio(estacion, "estacion", MODULO);
        return repositorio.buscarEstacion(estacion.trim());
    }

    public IncidenteVial reportarIncidenteVial(String zona, String tipo, String descripcion,
            String severidad, String reportadoPor) throws ErrorAplicacion {

        ValidadorEntrada.validarTextoObligatorio(zona, "zona", MODULO);
        ValidadorEntrada.validarTextoObligatorio(tipo, "tipo", MODULO);
        ValidadorEntrada.validarTextoObligatorio(descripcion, "descripcion", MODULO);
        ValidadorEntrada.validarTextoObligatorio(severidad, "severidad", MODULO);
        ValidadorEntrada.validarTextoObligatorio(reportadoPor, "reportadoPor", MODULO);

        return repositorio.guardarIncidente(
                zona.trim(),
                tipo.trim(),
                descripcion.trim(),
                severidad.trim(),
                reportadoPor.trim()
        );
    }
}
