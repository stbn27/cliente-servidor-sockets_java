package servidor.validacion;

import servidor.excepciones.ErrorValidacion;

/**
 * Validaciones simples para la replica.
 */
public final class ValidadorEntrada {

    private ValidadorEntrada() {
    }

    public static void validarTextoObligatorio(String valor, String campo, String modulo)
            throws ErrorValidacion {
        if (valor == null || valor.trim().isEmpty()) {
            throw new ErrorValidacion(
                    "El campo '" + campo + "' es obligatorio.",
                    "Valor vacio o nulo para '" + campo + "'.",
                    modulo
            );
        }
        if (valor.contains("|")) {
            throw new ErrorValidacion(
                    "El campo '" + campo + "' contiene un caracter no permitido.",
                    "Se detecto el separador '|'.",
                    modulo
            );
        }
    }

    public static void validarEnteroPositivo(int valor, String campo, String modulo)
            throws ErrorValidacion {
        if (valor <= 0) {
            throw new ErrorValidacion(
                    "El campo '" + campo + "' debe ser mayor que cero.",
                    "Valor recibido: " + valor,
                    modulo
            );
        }
    }
}
