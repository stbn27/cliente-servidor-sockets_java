package servidor.servicios;

import servidor.util.ValidadorSalud;

/**
 * Servicio adicional propuesto:
 * clasificación básica de glucosa en ayuno.
 */
public class ServicioGlucosa {

    public String diagnosticar(int glucosaMgDl) {
        ValidadorSalud.validarGlucosa(glucosaMgDl);

        String diagnostico;

        if (glucosaMgDl < 70) {
            diagnostico = "Glucosa baja";
        } else if (glucosaMgDl <= 99) {
            diagnostico = "Glucosa normal en ayuno";
        } else if (glucosaMgDl <= 125) {
            diagnostico = "Prediabetes";
        } else {
            diagnostico = "Valor alto, posible diabetes";
        }

        return String.format(
                "Glucosa en ayuno: %d mg/dL | Diagnóstico: %s",
                glucosaMgDl,
                diagnostico
        );
    }
}