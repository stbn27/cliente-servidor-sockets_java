package servidor.repositorios;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import servidor.excepciones.ErrorNoEncontrado;
import servidor.excepciones.ErrorPersistencia;
import servidor.modelos.EstacionEcoBici;
import servidor.modelos.IncidenteVial;
import servidor.modelos.TraficoZona;
import servidor.persistencia.ArchivoCsvUtil;

/**
 * Repositorio de la categoria movilidad urbana.
 */
public final class RepositorioMovilidadUrbana {

    private static final String MODULO = "MOVILIDAD_URBANA";
    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final Path archivoTrafico;
    private final Path archivoEstaciones;
    private final Path archivoIncidentes;

    public RepositorioMovilidadUrbana(Path directorioBase) {
        this.archivoTrafico = directorioBase.resolve("trafico.csv");
        this.archivoEstaciones = directorioBase.resolve("estaciones.csv");
        this.archivoIncidentes = directorioBase.resolve("incidentes.csv");
    }

    public TraficoZona buscarTraficoPorZona(String zona) throws ErrorNoEncontrado, ErrorPersistencia {
        List<String[]> registros = ArchivoCsvUtil.leerRegistros(archivoTrafico, MODULO);

        for (String[] columnas : registros) {
            if (columnas[0].equalsIgnoreCase(zona)) {
                return new TraficoZona(columnas[0], columnas[1], Integer.parseInt(columnas[2]), columnas[3]);
            }
        }

        throw new ErrorNoEncontrado(
                "La zona solicitada no existe en el monitoreo vial.",
                "Zona inexistente: " + zona,
                MODULO
        );
    }

    public EstacionEcoBici buscarEstacion(String estacion) throws ErrorNoEncontrado, ErrorPersistencia {
        List<String[]> registros = ArchivoCsvUtil.leerRegistros(archivoEstaciones, MODULO);

        for (String[] columnas : registros) {
            if (columnas[0].equalsIgnoreCase(estacion)) {
                return new EstacionEcoBici(columnas[0], Integer.parseInt(columnas[1]),
                        Integer.parseInt(columnas[2]), columnas[3]);
            }
        }

        throw new ErrorNoEncontrado(
                "La estacion solicitada no existe en el sistema EcoBici.",
                "Estacion inexistente: " + estacion,
                MODULO
        );
    }

    public synchronized IncidenteVial guardarIncidente(String zona,
                                                       String tipo,
                                                       String descripcion,
                                                       String severidad,
                                                       String reportadoPor
    ) throws ErrorPersistencia {

        List<IncidenteVial> incidentes = leerIncidentes();
        int siguienteId = 1;
        for (IncidenteVial incidente : incidentes) {
            if (incidente.getIdentificador() >= siguienteId) {
                siguienteId = incidente.getIdentificador() + 1;
            }
        }

        IncidenteVial nuevoIncidente = new IncidenteVial(
                siguienteId,
                zona,
                tipo,
                descripcion,
                LocalDateTime.now().format(FORMATO_FECHA),
                severidad,
                reportadoPor
        );

        incidentes.add(nuevoIncidente);
        guardarIncidentes(incidentes);

        return nuevoIncidente;
    }

    private List<IncidenteVial> leerIncidentes() throws ErrorPersistencia {

        List<String[]> registros = ArchivoCsvUtil.leerRegistros(archivoIncidentes, MODULO);
        List<IncidenteVial> incidentes = new ArrayList<>();

        for (String[] columnas : registros) {
            incidentes.add(new IncidenteVial(
                    Integer.parseInt(columnas[0]),
                    columnas[1],
                    columnas[2],
                    columnas[3],
                    columnas[4],
                    columnas[5],
                    columnas[6]));
        }

        return incidentes;
    }

    private void guardarIncidentes(List<IncidenteVial> incidentes) throws ErrorPersistencia {

        List<String> lineas = new ArrayList<>();
        lineas.add("id|zona|tipo|descripcion|fechaHora|severidad|reportadoPor");

        for (IncidenteVial incidente : incidentes) {
            lineas.add(incidente.getIdentificador() + "|"
                    + incidente.toHashtable().get("zona") + "|"
                    + incidente.toHashtable().get("tipo") + "|"
                    + incidente.toHashtable().get("descripcion") + "|"
                    + incidente.toHashtable().get("fechaHora") + "|"
                    + incidente.toHashtable().get("severidad") + "|"
                    + incidente.toHashtable().get("reportadoPor"));
        }

        ArchivoCsvUtil.escribirArchivo(archivoIncidentes, lineas, MODULO);
    }
}
