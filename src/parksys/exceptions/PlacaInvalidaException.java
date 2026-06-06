package parksys.exceptions;

/*
 * Lançada quando a placa digitada não segue o formato aceito.
 * Formatos válidos:
 *   - Padrão antigo: ABC1234  (3 letras + 4 números)
 *   - Mercosul:      ABC1D23  (3 letras + 1 número + 1 letra + 2 números)
 */
public class PlacaInvalidaException extends Exception {

    /*
     * A mensagem mostra a placa inválida e os formatos aceitos.
     * Uso: throw new PlacaInvalidaException("abc-12");
     */
    public PlacaInvalidaException(String placa) {
        super("Placa inválida: \"" + placa + "\"."
            + " Use ABC1234 (padrão) ou ABC1D23 (Mercosul).");
    }
}
