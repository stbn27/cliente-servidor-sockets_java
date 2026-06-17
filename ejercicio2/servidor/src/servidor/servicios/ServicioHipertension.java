package servidor.servicios;

import servidor.util.ValidadorSalud;

/**
 * Lógica de diagnóstico de hipertensión.
 */
public class ServicioHipertension {

    public String diagnosticar(int sistolica, int diastolica) {
        ValidadorSalud.validarPresion(sistolica, diastolica);

        String diagnostico;

        if (sistolica < 120 && diastolica < 80) {
            diagnostico = "Normal";
        } else if (sistolica >= 120 && sistolica <= 129 && diastolica < 80) {
            diagnostico = "Elevada";
        } else if ((sistolica >= 130 && sistolica <= 139) || (diastolica >= 80 && diastolica <= 89)) {
            diagnostico = "Hipertensión etapa 1";
        } else if (sistolica >= 140 || diastolica >= 90) {
            diagnostico = "Hipertensión etapa 2";
        } else {
            diagnostico = "Lectura no clasificada";
        }

        return String.format(
                "Presión registrada: %d/%d mmHg | Diagnóstico: %s",
                sistolica,
                diastolica,
                diagnostico
        );
    }
}