package servidor;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.Year;
import java.util.HashMap;
import java.util.Map;

/**
 * Servidor TCP para procesar CURP o matrícula UAM.
 *
 * @author esteban
 * @version 1.0
 */
public class Servidor {

    private static final int PUERTO = 2000;

    /**
     * Método principal del servidor.
     *
     * @param args argumentos de la línea de comandos.
     */
    public static void main(String[] args) {

        try (ServerSocket servidor = new ServerSocket(PUERTO)) {

            System.out.println("Servidor iniciado en el puerto " + PUERTO);
            System.out.println("Esperando cliente...");

            try (Socket cliente = servidor.accept(); DataInputStream entrada = new DataInputStream(cliente.getInputStream()); DataOutputStream salida = new DataOutputStream(cliente.getOutputStream())) {

                System.out.println("Cliente conectado");

                boolean continuar = true;

                // Itera hasta que el usuario cliente envie SALIR
                while (continuar) {

                    // Primer mensaje del cliente
                    String mensaje = entrada.readUTF();

                    System.out.println("Mensaje recibido: " + mensaje);

                    if (mensaje.equalsIgnoreCase("SALIR")) {
                        // Verifica si el mensaje fue SALIR y terminar la conexion
                        String respuesta = "Conexión finalizada";
                        
                        salida.writeUTF(respuesta);
                        System.out.println("Respuesta enviada: " + respuesta);
                        
                        continuar = false;
                    } else {
                        // Procesamiento del mensaje
                        String respuesta = procesarMensaje(mensaje);
                        
                        // Envio del mensaje al cliente
                        salida.writeUTF(respuesta);
                        
                        System.out.println("Respuesta enviada: " + respuesta);
                    }
                }

            }

            System.out.println("Cliente desconectado");

        } catch (IOException e) {
            System.err.println("ERR EN_EL_SERVIDOR - " + e.getMessage());
        }
    }

    /**
     * Procesa el mensaje recibido desde el cliente. El formato esperado es: C
     * CURP M MATRICULA
     *
     * @param mensaje mensaje enviado por el cliente.
     * @return respuesta procesada o código de error.
     */
    private static String procesarMensaje(String mensaje) {

        // Que no se null y minimo un caracter distito de espacio
        if (mensaje == null || mensaje.trim().isEmpty()) {
            return "ERR FORMATO_INVALIDO";
        }

        // Divide la cadena por uno o mas espacios
        // [\s -> cualquier caracter de espacio; + \s tantas veces como aparesca \s]
        String[] partes = mensaje.trim().split("\\s+");

        // Obligatoriamente tiene que venir la inicial + el valor (M 2203045231)
        if (partes.length != 2) {
            return "ERR FORMATO_INVALIDO";
        }

        // Convertimos a mayuscula para comparar mas facilmente
        String tipo = partes[0].toUpperCase();
        String dato = partes[1].toUpperCase();

        // Determinamos el tipo de solicitud
        return switch (tipo) {
            case "C" ->
                procesarCurp(dato);
            case "M" ->
                procesarMatricula(dato);
            default ->
                "ERR TIPO_INVALIDO";
        };
    }

    /**
     * Procesa una CURP.
     *
     * @param curp CURP recibida.
     * @return información extraída de la CURP o código de error.
     */
    private static String procesarCurp(String curp) {

        // Valida que no sea NULL y sean 18 caracteres
        if (curp == null || curp.length() != 18) {
            return "ERR CURP_INVALIDA";
        }

        // Extraccion de la fecha de nacimiento
        String anioTexto = curp.substring(4, 6);
        String mesTexto = curp.substring(6, 8);
        String diaTexto = curp.substring(8, 10);
        
        // Extracción del sexo
        char sexoCaracter = curp.charAt(10);
        
        // Extraccion del Estado de Naciminto
        String entidadClave = curp.substring(11, 13);

        // Valida que los datos extraidos del nacimiento sean digitos y una longitud de 2
        // [\d -> cualquier digito del 0-9; {2} -> 2 repeticiones, longitud =2]
        if (!anioTexto.matches("\\d{2}")
                || !mesTexto.matches("\\d{2}")
                || !diaTexto.matches("\\d{2}")) {
            return "ERR CURP_INVALIDA";
        }

        // Obtener y validar el nombre del mes en texto; ejemplo: 01 -> Enero
        String mesNombre = obtenerMes(mesTexto);
        if (mesNombre == null) {
            return "ERR CURP_INVALIDA";
        }

        // Valida que sea un dia valido
        int dia = Integer.parseInt(diaTexto);
        if (dia < 1 || dia > 31) {
            return "ERR CURP_INVALIDA";
        }

        // Determina el sexo en formato texto
        String sexo;
        switch (sexoCaracter) {
            case 'H':
            case 'M':
                sexo = "masculino";
                break;

            case 'F':
                sexo = "femenino";
                break;

            case 'X':
                sexo = "no binario";
                break;

            default:
                return "ERR CURP_INVALIDA";
        }

        // Valida que exista la entidad
        Map<String, String> entidades = obtenerEntidades();
        if (!entidades.containsKey(entidadClave)) {
            return "ERR ENTIDAD_INVALIDA";
        }
        
        // Obtener la entidad deferativa
        String entidad = entidades.get(entidadClave);

        // Determinar el año de nacimiento
        int anioDosDigitos = Integer.parseInt(anioTexto);
        int anioActualDosDigitos = Year.now().getValue() % 100;

        int anioCompleto;

        if (anioDosDigitos > anioActualDosDigitos) {
            anioCompleto = 1900 + anioDosDigitos;
        } else {
            anioCompleto = 2000 + anioDosDigitos;
        }

        String fechaNacimiento = dia + " de " + mesNombre + " de " + anioCompleto;

        // Mensaje de salida para el cliente
        return "Fecha de nacimiento: " + fechaNacimiento
                + ", Sexo: " + sexo
                + ", Entidad federativa: " + entidad;
    }

    /**
     * Procesa una matrícula UAM.
     *
     * @param matricula matrícula recibida.
     * @return información extraída de la matrícula o código de error.
     */
    private static String procesarMatricula(String matricula) {

        // Validar que no se NULL, longitud 10, y todos sean digitos 0-9
        if (matricula == null || matricula.length() != 10 || !matricula.matches("\\d{10}")) {
            return "ERR MATRICULA_INVALIDA";
        }

        char unidadCaracter = matricula.charAt(0);
        String anioTexto = matricula.substring(1, 3);
        char trimestreCaracter = matricula.charAt(3);

        Map<Character, String> unidades = obtenerUnidades();
        // Verificar que la unidad exista
        if (!unidades.containsKey(unidadCaracter)) {
            return "ERR UNIDAD_INVALIDA";
        }

        Map<Character, String> trimestres = obtenerTrimestres();
        // Verificar que el trimestre exista
        if (!trimestres.containsKey(trimestreCaracter)) {
            return "ERR TRIMESTRE_INVALIDO";
        }

        // Determinar el año de de ingreso
        String unidad = unidades.get(unidadCaracter);
        int anioIngreso = 2000 + Integer.parseInt(anioTexto);
        String trimestre = trimestres.get(trimestreCaracter);

        // Mensaje de salida para el cliente
        return "Unidad: " + unidad
                + ", Año de ingreso: " + anioIngreso
                + ", Trimestre: " + trimestre;
    }

    /**
     * Obtiene el nombre del mes a partir de su número.
     *
     * @param numeroMes mes en formato numérico.
     * @return nombre del mes o null si no existe.
     */
    private static String obtenerMes(String numeroMes) {

        switch (numeroMes) {
            case "01":
                return "enero";

            case "02":
                return "febrero";

            case "03":
                return "marzo";

            case "04":
                return "abril";

            case "05":
                return "mayo";

            case "06":
                return "junio";

            case "07":
                return "julio";

            case "08":
                return "agosto";

            case "09":
                return "septiembre";

            case "10":
                return "octubre";

            case "11":
                return "noviembre";

            case "12":
                return "diciembre";

            default:
                return null;
        }
    }

    /**
     * Crea el mapa de entidades federativas.
     *
     * @return mapa con clave de entidad y nombre completo.
     */
    private static Map<String, String> obtenerEntidades() {

        Map<String, String> entidades = new HashMap<>();

        entidades.put("AS", "Aguascalientes");
        entidades.put("BC", "Baja California");
        entidades.put("BS", "Baja California Sur");
        entidades.put("CC", "Campeche");
        entidades.put("CL", "Coahuila");
        entidades.put("CM", "Colima");
        entidades.put("CS", "Chiapas");
        entidades.put("CH", "Chihuahua");
        entidades.put("DF", "Ciudad de México");
        entidades.put("DG", "Durango");
        entidades.put("GT", "Guanajuato");
        entidades.put("GR", "Guerrero");
        entidades.put("HG", "Hidalgo");
        entidades.put("JC", "Jalisco");
        entidades.put("MC", "Estado de México");
        entidades.put("MN", "Michoacán");
        entidades.put("MS", "Morelos");
        entidades.put("NT", "Nayarit");
        entidades.put("NL", "Nuevo León");
        entidades.put("OC", "Oaxaca");
        entidades.put("PL", "Puebla");
        entidades.put("QT", "Querétaro");
        entidades.put("QR", "Quintana Roo");
        entidades.put("SP", "San Luis Potosí");
        entidades.put("SL", "Sinaloa");
        entidades.put("SR", "Sonora");
        entidades.put("TC", "Tabasco");
        entidades.put("TS", "Tamaulipas");
        entidades.put("TL", "Tlaxcala");
        entidades.put("VZ", "Veracruz");
        entidades.put("YN", "Yucatán");
        entidades.put("ZS", "Zacatecas");
        entidades.put("NE", "Nacido en el extranjero");

        return entidades;
    }

    /**
     * Crea el mapa de unidades UAM.
     *
     * @return mapa con clave de unidad y nombre de unidad.
     */
    private static Map<Character, String> obtenerUnidades() {

        Map<Character, String> unidades = new HashMap<>();

        unidades.put('1', "Iztapalapa");
        unidades.put('2', "Azcapotzalco");
        unidades.put('3', "Xochimilco");
        unidades.put('4', "Cuajimalpa");
        unidades.put('5', "Lerma");

        return unidades;
    }

    /**
     * Crea el mapa de trimestres.
     *
     * @return mapa con clave de trimestre y nombre del trimestre.
     */
    private static Map<Character, String> obtenerTrimestres() {

        Map<Character, String> trimestres = new HashMap<>();

        trimestres.put('1', "Invierno");
        trimestres.put('2', "Primavera");
        trimestres.put('3', "Otoño");

        return trimestres;
    }
}
