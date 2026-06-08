package parksys.observer;
import parksys.enums.StatusVaga;

/*
 * Uso do padrão de projeto Observer.
 * 
 * Sem Observer, o GerenciadorEstacionamento precisaria conhecer cada tela
 * para atualizar diretamente ( tornaria o sistema difícil de manter).
 * Com Observer, o gerenciador só conhece a INTERFACE ( não as classes concretas).
 */
public interface EstacionamentoObserver {

    /*
     * É chamado pelo GerenciadorEstacionamento
     * sempre que uma vaga muda de status.
     *
     * idVaga: ID da vaga que mudou, ex: "A01"
     * novoStatus: novo status da vaga (LIVRE, OCUPADA ou RESERVADA)
     *
     * Quem implementa esta interface decide o que fazer com essa informação:
     *   - PainelMonitor -> imprime no console
     *   - tela Swing -> atualiza um label na interface gráfica
     */
    void onVagaAlterada(String idVaga, StatusVaga novoStatus);
}
