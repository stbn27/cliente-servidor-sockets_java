package cliente.rpc;

import java.util.Vector;
import org.apache.xmlrpc.XmlRpcClient;

/**
 * Encapsula las llamadas remotas al servidor XML-RPC.
 */
public class ClienteRpcSalud {

    private static final String HANDLER = "salud";

    private final XmlRpcClient cliente;

    public ClienteRpcSalud(String urlServidor) throws Exception {
        this.cliente = new XmlRpcClient(urlServidor);
    }

    @SuppressWarnings("unused")
    public String diagnosticarImc(int pesoKg, double estaturaMetros) throws Exception {
        Vector<Object> parametros = new Vector<>();

        parametros.addElement(pesoKg);
        parametros.addElement(estaturaMetros);

        Object respuesta = cliente.execute(HANDLER + ".diagnosticarImc", parametros);
        return (String) respuesta;
    }

    @SuppressWarnings("unused")
    public String diagnosticarHipertension(int sistolica, int diastolica) throws Exception {

        Vector<Integer> parametros = new Vector<>();

        parametros.addElement(sistolica);
        parametros.addElement(diastolica);

        Object respuesta = cliente.execute(HANDLER + ".diagnosticarHipertension", parametros);
        return (String) respuesta;
    }

    @SuppressWarnings("unused")
    public String diagnosticarGlucosaAyuno(int glucosaMgDl) throws Exception {

        Vector<Integer> parametros = new Vector<>();

        parametros.addElement(glucosaMgDl);

        Object respuesta = cliente.execute(HANDLER + ".diagnosticarGlucosaAyuno", parametros);
        return (String) respuesta;
    }
}