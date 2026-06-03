package servidor.repositorio;

import servidor.modelo.Asiento;
import servidor.modelo.Boleto;
import servidor.modelo.EstadoAsiento;
import servidor.modelo.FuncionCine;
import servidor.modelo.Pelicula;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Gestiona la persistencia del servidor usando archivos de texto plano.
 */
public class ArchivoRepository {
    private final Path dataDir;
    private final Path peliculasFile;
    private final Path funcionesFile;
    private final Path asientosFile;
    private final Path boletosFile;

    /**
     * Crea el repositorio usando la carpeta {@code data/}.
     */
    public ArchivoRepository() {
        this(Path.of("data"));
    }

    /**
     * Crea el repositorio usando una carpeta especifica.
     *
     * @param dataDir carpeta raiz de datos.
     */
    public ArchivoRepository(Path dataDir) {
        this.dataDir = dataDir;
        this.peliculasFile = dataDir.resolve("peliculas.txt");
        this.funcionesFile = dataDir.resolve("funciones.txt");
        this.asientosFile = dataDir.resolve("asientos.txt");
        this.boletosFile = dataDir.resolve("boletos.txt");
    }

    /**
     * Garantiza que existan los archivos base del sistema con datos iniciales.
     *
     * @throws IOException si ocurre un error de lectura o escritura.
     */
    public void asegurarArchivos() throws IOException {
        Files.createDirectories(dataDir);
        inicializarPeliculasSiEsNecesario();
        inicializarFuncionesSiEsNecesario();
        inicializarAsientosSiEsNecesario();
        inicializarBoletosSiEsNecesario();
    }

    /**
     * Carga las peliculas desde el archivo de datos.
     *
     * @return mapa de peliculas indexadas por identificador.
     * @throws IOException si ocurre un error de lectura.
     */
    public Map<String, Pelicula> cargarPeliculas() throws IOException {
        Map<String, Pelicula> peliculas = new LinkedHashMap<>();
        for (String linea : leerLineas(peliculasFile)) {
            if (linea.isBlank()) {
                continue;
            }
            String[] partes = linea.split("\\|");
            if (partes.length != 4) {
                continue;
            }
            Pelicula pelicula = new Pelicula(partes[0].trim(), partes[1].trim(), partes[2].trim(),
                    Integer.parseInt(partes[3].trim()));
            peliculas.put(pelicula.getIdPelicula(), pelicula);
        }
        return peliculas;
    }

    /**
     * Carga las funciones desde el archivo de datos.
     *
     * @return mapa de funciones indexadas por identificador.
     * @throws IOException si ocurre un error de lectura.
     */
    public Map<String, FuncionCine> cargarFunciones() throws IOException {
        Map<String, FuncionCine> funciones = new LinkedHashMap<>();
        for (String linea : leerLineas(funcionesFile)) {
            if (linea.isBlank()) {
                continue;
            }
            String[] partes = linea.split("\\|");
            if (partes.length != 5) {
                continue;
            }
            FuncionCine funcion = new FuncionCine(partes[0].trim(), partes[1].trim(), partes[2].trim(),
                    partes[3].trim(), partes[4].trim());
            funciones.put(funcion.getIdFuncion(), funcion);
        }
        return funciones;
    }

    /**
     * Carga los asientos agrupados por funcion.
     *
     * @return mapa de asientos por identificador de funcion.
     * @throws IOException si ocurre un error de lectura.
     */
    public Map<String, Map<String, Asiento>> cargarAsientos() throws IOException {
        Map<String, Map<String, Asiento>> asientos = new LinkedHashMap<>();
        for (String linea : leerLineas(asientosFile)) {
            if (linea.isBlank()) {
                continue;
            }
            String[] partes = linea.split("\\|");
            if (partes.length != 3) {
                continue;
            }
            String idFuncion = partes[0].trim();
            Asiento asiento = new Asiento(idFuncion, partes[1].trim(), EstadoAsiento.valueOf(partes[2].trim()));
            asientos.computeIfAbsent(idFuncion, ignored -> new LinkedHashMap<>()).put(asiento.getNumero(), asiento);
        }
        return asientos;
    }

    /**
     * Carga los boletos vendidos desde el archivo de datos.
     *
     * @return lista de boletos existentes.
     * @throws IOException si ocurre un error de lectura.
     */
    public List<Boleto> cargarBoletos() throws IOException {
        List<Boleto> boletos = new ArrayList<>();
        for (String linea : leerLineas(boletosFile)) {
            if (linea.isBlank()) {
                continue;
            }
            String[] partes = linea.split("\\|");
            if (partes.length != 6) {
                continue;
            }
            boletos.add(new Boleto(partes[0].trim(), partes[1].trim(), partes[2].trim(), partes[3].trim(),
                    partes[4].trim(), partes[5].trim()));
        }
        return boletos;
    }

    /**
     * Guarda el estado completo de los asientos.
     *
     * @param asientos mapa de asientos agrupados por funcion.
     * @throws IOException si ocurre un error de escritura.
     */
    public void guardarAsientos(Map<String, Map<String, Asiento>> asientos) throws IOException {
        List<String> lineas = new ArrayList<>();
        for (Map<String, Asiento> asientosFuncion : asientos.values()) {
            for (Asiento asiento : asientosFuncion.values()) {
                lineas.add(asiento.getIdFuncion() + "|" + asiento.getNumero() + "|" + asiento.getEstado().name());
            }
        }
        escribirLineas(asientosFile, lineas);
    }

    /**
     * Guarda todos los boletos vendidos.
     *
     * @param boletos lista de boletos a persistir.
     * @throws IOException si ocurre un error de escritura.
     */
    public void guardarBoletos(List<Boleto> boletos) throws IOException {
        List<String> lineas = new ArrayList<>();
        for (Boleto boleto : boletos) {
            lineas.add(boleto.getIdBoleto() + "|" + boleto.getIdFuncion() + "|" + boleto.getAsiento() + "|"
                    + boleto.getNombreCliente() + "|" + boleto.getFechaCompra() + "|" + boleto.getHoraCompra());
        }
        escribirLineas(boletosFile, lineas);
    }

    /**
     * Obtiene la ruta base del directorio de datos.
     *
     * @return ruta base del directorio de datos.
     */
    public Path getDataDir() {
        return dataDir;
    }

    /**
     * Inicializa el archivo de peliculas cuando no existe o esta vacio.
     *
     * @throws IOException si ocurre un error de escritura.
     */
    private void inicializarPeliculasSiEsNecesario() throws IOException {
        if (!Files.exists(peliculasFile) || Files.size(peliculasFile) == 0L) {
            escribirLineas(peliculasFile, crearPeliculasIniciales());
        }
    }

    /**
     * Inicializa el archivo de funciones cuando no existe o esta vacio.
     *
     * @throws IOException si ocurre un error de escritura.
     */
    private void inicializarFuncionesSiEsNecesario() throws IOException {
        if (!Files.exists(funcionesFile) || Files.size(funcionesFile) == 0L) {
            escribirLineas(funcionesFile, crearFuncionesIniciales());
        }
    }

    /**
     * Inicializa el archivo de asientos cuando no existe o esta vacio.
     *
     * @throws IOException si ocurre un error de escritura.
     */
    private void inicializarAsientosSiEsNecesario() throws IOException {
        if (!Files.exists(asientosFile) || Files.size(asientosFile) == 0L) {
            escribirLineas(asientosFile, crearAsientosIniciales(leerLineas(funcionesFile)));
        }
    }

    /**
     * Inicializa el archivo de boletos cuando no existe.
     *
     * @throws IOException si ocurre un error de escritura.
     */
    private void inicializarBoletosSiEsNecesario() throws IOException {
        if (!Files.exists(boletosFile)) {
            escribirLineas(boletosFile, List.of());
        }
    }

    /**
     * Construye las peliculas iniciales del sistema.
     *
     * @return lineas iniciales de peliculas.
     */
    private List<String> crearPeliculasIniciales() {
        return List.of(
                "P001|Interestelar|B|169",
                "P002|Spider-Man|A|120",
                "P003|El Conjuro|C|112"
        );
    }

    /**
     * Construye las funciones iniciales tomando como base la fecha actual del servidor.
     *
     * @return lineas iniciales de funciones.
     */
    private List<String> crearFuncionesIniciales() {
        LocalDate hoy = LocalDate.now();
        return List.of(
                "F001|P001|Sala 1|" + hoy + "|18:00",
                "F002|P001|Sala 1|" + hoy + "|21:00",
                "F003|P002|Sala 2|" + hoy + "|17:00",
                "F004|P003|Sala 3|" + hoy.plusDays(1) + "|20:00",
                "F005|P002|Sala 2|" + hoy.plusDays(2) + "|19:00"
        );
    }

    /**
     * Construye los asientos iniciales a partir de las funciones existentes.
     *
     * @param lineasFunciones lineas crudas del archivo de funciones.
     * @return lineas iniciales de asientos.
     */
    private List<String> crearAsientosIniciales(List<String> lineasFunciones) {
        List<String> lineas = new ArrayList<>();
        for (String lineaFuncion : lineasFunciones) {
            if (lineaFuncion == null || lineaFuncion.isBlank()) {
                continue;
            }
            String[] partes = lineaFuncion.split("\\|");
            if (partes.length < 1) {
                continue;
            }
            String idFuncion = partes[0].trim();
            for (int indice = 1; indice <= 5; indice++) {
                lineas.add(idFuncion + "|A" + indice + "|DISPONIBLE");
            }
        }
        return lineas;
    }

    /**
     * Lee todas las lineas de un archivo usando UTF-8.
     *
     * @param archivo archivo a leer.
     * @return lineas leidas.
     * @throws IOException si ocurre un error de lectura.
     */
    private List<String> leerLineas(Path archivo) throws IOException {
        if (!Files.exists(archivo)) {
            return List.of();
        }
        return Files.readAllLines(archivo, StandardCharsets.UTF_8);
    }

    /**
     * Escribe todas las lineas de un archivo usando UTF-8.
     *
     * @param archivo archivo destino.
     * @param lineas lineas a escribir.
     * @throws IOException si ocurre un error de escritura.
     */
    private void escribirLineas(Path archivo, List<String> lineas) throws IOException {
        Files.write(archivo, lineas, StandardCharsets.UTF_8);
    }
}
