package parksys.services;

import parksys.entities.Vaga;
import parksys.entities.Vaga;
import parksys.enums.StatusVaga;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

// ger. central do estacionamento - sem singleton e sem synchronized nesta branch
// singleton vem em feature/patterns; synchronized em feature/threads
public class GerenciadorEstacionamento {

    // c01: hashmap<string,vaga> - lookup O(1) p/ id da vaga
    // chave = cod  (ex: "A01"), val = obj Vaga c/ status atual
    // arraylist exigiria iter. linear O(n) p/ achar vaga por id - inaceitavel
    private HashMap<String, Vaga> vagas;

    public GerenciadorEstacionamento() {
        vagas = new HashMap<>();
        inicializarVagas();
    }

    // cria as 30 vagas do estacionamento (A01-A15, B01-B15) e popula o map
    // String.format("%02d", i) = padding c/ zero: 1 -> "01", 15 -> "15"
    private void inicializarVagas() {
        for (int i = 1; i <= 15; i++) {
            String n = String.format("%02d", i);
            vagas.put("A" + n, new Vaga("A" + n));
            vagas.put("B" + n, new Vaga("B" + n));
        }
    }

    //evita modificações externas nas structs internas
    public Map<String, Vaga> getVagas() {
        return Collections.unmodifiableMap(vagas);
    }
}
