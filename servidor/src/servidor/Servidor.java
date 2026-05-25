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

            try (Socket cliente = servidor.accept();
                 DataInputStream entrada = new DataInputStream(cliente.getInputStream());
                 DataOutputStream salida = new DataOutputStream(cliente.getOutputStream())) {

                System.out.println("Cliente conectado");

                boolean continuar = true;

                while (continuar) {
                    String mensaje = entrada.readUTF();

                    System.out.println("Mensaje recibido: " + mensaje);

                    if (mensaje.equalsIgnoreCase("SALIR")) {
                        String respuesta = "Conexión finalizada";
                        salida.writeUTF(respuesta);
                        System.out.println("Respuesta enviada: " + respuesta);
                        continuar = false;
                    } else {
                        String respuesta = procesarMensaje(mensaje);
                        salida.writeUTF(respuesta);
                        System.out.println("Respuesta enviada: " + respuesta);
                    }
                }

            }

            System.out.println("Cliente desconectado");

        } catch (IOException e) {
            System.out.println("Error en el servidor: " + e.getMessage());
        }
    }

    /**
     * Procesa el mensaje recibido desde el cliente.
     * El formato esperado es:
     * C CURP
     * M MATRICULA
     *
     * @param mensaje mensaje enviado por el cliente.
     * @return respuesta procesada o código de error.
     */
    private static String procesarMensaje(String mensaje) {

        if (mensaje == null || mensaje.trim().isEmpty()) {
            return "ERR FORMATO_INVALIDO";
        }

        String[] partes = mensaje.trim().split("\\s+");

        if (partes.length != 2) {
            return "ERR FORMATO_INVALIDO";
        }

        String tipo = partes[0].toUpperCase();
        String dato = partes[1].toUpperCase();

        return switch (tipo) {
            case "C" -> procesarCurp(dato);
            case "M" -> procesarMatricula(dato);
            default -> "ERR TIPO_INVALIDO";
        };
    }

    /**
     * Procesa una CURP.
     *
     * @param curp CURP recibida.
     * @return información extraída de la CURP o código de error.
     */
    private static String procesarCurp(String curp) {

        if (curp == null || curp.length() != 18) {
            return "ERR CURP_INVALIDA";
        }

        String anioTexto = curp.substring(4, 6);
        String mesTexto = curp.substring(6, 8);
        String diaTexto = curp.substring(8, 10);
        char sexoCaracter = curp.charAt(10);
        String entidadClave = curp.substring(11, 13);

        if (!anioTexto.matches("\\d{2}") ||
                !mesTexto.matches("\\d{2}") ||
                !diaTexto.matches("\\d{2}")) {
            return "ERR CURP_INVALIDA";
        }

        String mesNombre = obtenerMes(mesTexto);

        if (mesNombre == null) {
            return "ERR CURP_INVALIDA";
        }

        int dia = Integer.parseInt(diaTexto);

        if (dia < 1 || dia > 31) {
            return "ERR CURP_INVALIDA";
        }

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

        Map<String, String> entidades = obtenerEntidades();

        if (!entidades.containsKey(entidadClave)) {
            return "ERR ENTIDAD_INVALIDA";
        }

        String entidad = entidades.get(entidadClave);

        int anioDosDigitos = Integer.parseInt(anioTexto);
        int anioActualDosDigitos = Year.now().getValue() % 100;

        int anioCompleto;

        if (anioDosDigitos > anioActualDosDigitos) {
            anioCompleto = 1900 + anioDosDigitos;
        } else {
            anioCompleto = 2000 + anioDosDigitos;
        }

        String fechaNacimiento = dia + " de " + mesNombre + " de " + anioCompleto;

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

        if (matricula == null || matricula.length() != 10 || !matricula.matches("\\d{10}")) {
            return "ERR MATRICULA_INVALIDA";
        }

        char unidadCaracter = matricula.charAt(0);
        String anioTexto = matricula.substring(1, 3);
        char trimestreCaracter = matricula.charAt(3);

        Map<Character, String> unidades = obtenerUnidades();

        if (!unidades.containsKey(unidadCaracter)) {
            return "ERR UNIDAD_INVALIDA";
        }

        Map<Character, String> trimestres = obtenerTrimestres();

        if (!trimestres.containsKey(trimestreCaracter)) {
            return "ERR TRIMESTRE_INVALIDO";
        }

        String unidad = unidades.get(unidadCaracter);
        int anioIngreso = 2000 + Integer.parseInt(anioTexto);
        String trimestre = trimestres.get(trimestreCaracter);

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