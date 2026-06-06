package parksys.exceptions;

/*
 * Lançada quando buscamos um veículo pela placa mas ele não está
 * registrado como "em andamento" no estacionamento.
 * Por exemplo: tentar registrar saída de uma placa que não entrou.
 */
public class VeiculoNaoEncontradoException extends Exception {

    /*
     * A mensagem inclui a placa para facilitar o diagnóstico.
     * Uso: throw new VeiculoNaoEncontradoException("XYZ9999");
     */
    public VeiculoNaoEncontradoException(String placa) {
        super("Veículo com placa " + placa
            + " não encontrado no estacionamento.");
    }
}
