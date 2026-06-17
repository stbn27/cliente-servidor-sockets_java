package servidor.util;

/**
 * Validador centralizado para entradas de los servicios.
 */
public final class ValidadorSalud {

    private ValidadorSalud() {
    }

    public static void validarPeso(int pesoKg) {
        if (pesoKg <= 0) {
            throw new IllegalArgumentException("El peso debe ser mayor a 0 kg.");
        }
    }

    public static void validarEstatura(double estaturaMetros) {
        if (estaturaMetros <= 0) {
            throw new IllegalArgumentException("La estatura debe ser mayor a 0 metros.");
        }
    }

    public static void validarPresion(int sistolica, int diastolica) {
        if (sistolica <= 0 || diastolica <= 0) {
            throw new IllegalArgumentException("Los valores de presión deben ser mayores a 0.");
        }
    }

    public static void validarGlucosa(int glucosaMgDl) {
        if (glucosaMgDl <= 0) {
            throw new IllegalArgumentException("La glucosa debe ser mayor a 0 mg/dL.");
        }
    }
}