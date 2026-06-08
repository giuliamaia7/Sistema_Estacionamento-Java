package parksys.observer;

import parksys.enums.StatusVaga;
import java.util.HashMap;
import java.util.Collections;
import java.util.Map;

/*
 * PainelMonitor é o "assinante" do padrão Observer.
 * Ele implementa EstacionamentoObserver
 *
 * Toda vez que uma vaga muda de status, o GerenciadorEstacionamento
 * chama onVagaAlterada() de todos os observadores registrados.
 * O PainelMonitor guarda o status mais recente de cada vaga em um mapa.
 *
 */
public class PainelMonitor implements EstacionamentoObserver {

    /*
     * Mapa que guarda o status mais recente de cada vaga.
     * Chave: ID da vaga (String), ex: "A01"
     * Valor: StatusVaga (LIVRE, OCUPADA ou RESERVADA)
     *
     * É atualizado cada vez que onVagaAlterada() é chamado.
     * HashMap permite acesso rápido O(1) pelo ID da vaga.
     */
    private final Map<String, StatusVaga> mapaStatus = new HashMap<>();

    /*
     * Este método é chamado automaticamente pelo gerenciador
     * toda vez que uma vaga muda de status.
     *
     * idVaga: qual vaga mudou (ex: "B03")
     * novoStatus: o novo status (ex: StatusVaga.OCUPADA)
     */
    @Override
    public void onVagaAlterada(String idVaga, StatusVaga novoStatus) {
        // Atualiza (ou insere) o status desta vaga no mapa
        // put(chave, valor) - se a chave já existir, sobrescreve o valor
        mapaStatus.put(idVaga, novoStatus);

        // Imprime no console para acompanhamento em tempo real
        System.out.printf("[PainelMonitor] Vaga %-4s → %s%n",
                idVaga, novoStatus.getDescricao());
    }

    /*
     * Retorna uma visão somente-leitura do mapa.
     * unmodifiableMap() cria um wrapper que lança uma exceção
     * se alguém tentar modificar o mapa externamente — proteção dos dados.
     */
    public Map<String, StatusVaga> getMapaStatus() {
        return Collections.unmodifiableMap(mapaStatus);
    }

    /*
     * Conta quantas vagas têm um determinado status no mapa local.
     * Útil para exibir estatísticas.
     *
     * StatusVaga: status que queremos contar (ex: StatusVaga.LIVRE)
     * retorna a quantidade de vagas com esse status
     */
    public long contarPorStatus(StatusVaga status) {
        // stream(): transforma a coleção em um fluxo de elementos
        // filter(): filtra apenas os elementos que satisfazem a condição
        // count(): conta quantos sobraram após o filtro
        return mapaStatus.values().stream()
                .filter(s -> s == status)
                .count();
    }
}
