package servidor.servicios;

import servidor.util.ValidadorSalud;

/**
 * Lógica de diagnóstico por IMC.
 */
public class ServicioImc {

    public String diagnosticar(int pesoKg, double estaturaMetros) {

        ValidadorSalud.validarPeso(pesoKg);
        ValidadorSalud.validarEstatura(estaturaMetros);

        double imc = pesoKg / (estaturaMetros * estaturaMetros);
        String diagnostico;

        if (imc < 18.5) {
            diagnostico = "Bajo peso";
        } else if (imc < 25.0) {
            diagnostico = "Peso normal";
        } else if (imc < 30.0) {
            diagnostico = "Sobrepeso";
        } else if (imc < 35.0) {
            diagnostico = "Obesidad grado I";
        } else if (imc < 40.0) {
            diagnostico = "Obesidad grado II";
        } else {
            diagnostico = "Obesidad grado III";
        }

        return String.format(
                "Resultado IMC: %.2f | Diagnóstico: %s",
                imc,
                diagnostico
        );
    }
}