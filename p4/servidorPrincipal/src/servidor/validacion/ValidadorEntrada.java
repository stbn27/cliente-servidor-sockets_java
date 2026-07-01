package servidor.validacion;

import servidor.excepciones.ErrorValidacion;
import servidor.util.ConsolaServidor;

/**
 * Reune validaciones simples reutilizables para parametros de consola y RPC.
 */
public final class ValidadorEntrada {

    private ValidadorEntrada() {
    }

    /**
     * Valida un texto obligatorio.
     *
     * @param valor valor recibido.
     * @param campo nombre funcional del campo.
     * @param modulo modulo donde se valida.
     * @throws ErrorValidacion si el texto esta vacio o nulo.
     */
    public static void validarTextoObligatorio(String valor, String campo, String modulo)
            throws ErrorValidacion {

        if (valor == null || valor.trim().isEmpty()) {
            ConsolaServidor.error(campo, "El valor esta vacio o nulo para el campo");
            throw new ErrorValidacion(
                    "El campo '" + campo + "' es obligatorio.",
                    "Valor vacio o nulo para el campo '" + campo + "'.",
                    modulo
            );
        }

        if (valor.contains("|")) {
            ConsolaServidor.error(campo, "El campo '" + campo + "' contiene un caracter no permitido.");
            throw new ErrorValidacion(
                    "El campo '" + campo + "' contiene un caracter no permitido.",
                    "El separador '|' no puede enviarse en la entrada.",
                    modulo
            );
        }
    }

    /**
     * Valida un entero positivo.
     *
     * @param valor valor numerico.
     * @param campo nombre del campo.
     * @param modulo modulo donde se aplica la validacion.
     * @throws ErrorValidacion si el numero es cero o negativo.
     */
    public static void validarEnteroPositivo(int valor, String campo, String modulo)
            throws ErrorValidacion {

        if (valor <= 0) {
            ConsolaServidor.error(campo, "El campo '" + campo + "' debe ser mayor a cero.");
            throw new ErrorValidacion(
                    "El campo '" + campo + "' debe ser mayor que cero.",
                    "Valor recibido: " + valor,
                    modulo
            );
        }
    }
}
