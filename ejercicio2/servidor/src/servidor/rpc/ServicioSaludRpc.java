package servidor.rpc;

import servidor.servicios.ServicioGlucosa;
import servidor.servicios.ServicioHipertension;
import servidor.servicios.ServicioImc;

/**
 * Clase expuesta por XML-RPC.
 * Sus métodos públicos son invocables remotamente.
 */
public class ServicioSaludRpc {

    private final ServicioImc servicioImc;
    private final ServicioHipertension servicioHipertension;
    private final ServicioGlucosa servicioGlucosa;

    public ServicioSaludRpc() {
        this.servicioImc = new ServicioImc();
        this.servicioHipertension = new ServicioHipertension();
        this.servicioGlucosa = new ServicioGlucosa();
    }

    @SuppressWarnings("unused")
    public String diagnosticarImc(int pesoKg, double estaturaMetros) {
        return servicioImc.diagnosticar(pesoKg, estaturaMetros);
    }

    @SuppressWarnings("unused")
    public String diagnosticarHipertension(int sistolica, int diastolica) {
        return servicioHipertension.diagnosticar(sistolica, diastolica);
    }

    @SuppressWarnings("unused")
    public String diagnosticarGlucosaAyuno(int glucosaMgDl) {
        return servicioGlucosa.diagnosticar(glucosaMgDl);
    }
}