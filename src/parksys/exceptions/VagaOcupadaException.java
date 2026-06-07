package parksys.exceptions;

/*
 * Lançada quando tentamos alocar uma vaga que já está OCUPADA ou RESERVADA.
 */
public class VagaOcupadaException extends Exception {

    /*
     * Construtor recebe o ID da vaga para incluir na mensagem de erro.
     * super(...) chama o construtor da classe pai Exception,
     * definindo a mensagem que exception.getMessage() vai retornar.
     *
     * Uso: throw new VagaOcupadaException("A01");
     * Resultado: "Vaga A01 já está ocupada ou reservada."
     */
    public VagaOcupadaException(String idVaga) {
        super("Vaga " + idVaga + " já está ocupada ou reservada.");
    }
}
