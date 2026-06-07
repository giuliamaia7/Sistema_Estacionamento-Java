package parksys.services;

import parksys.entities.Mensalista;
import parksys.entities.Registro;
import parksys.entities.Vaga;
import parksys.enums.StatusVaga;
import parksys.exceptions.VagaOcupadaException;
import parksys.exceptions.VeiculoNaoEncontradoException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Iterator;
import java.util.LinkedList;

public class GerenciadorEstacionamento {

    // c01: hashmap<string,vaga> - lookup O(1) p/ id da vaga
    // chave = cod  (ex: "A01"), val = obj Vaga c/ status atual
    // arraylist exigiria iter. linear O(n) p/ achar vaga por id - inaceitavel
    private HashMap<String, Vaga> vagas;
    // c02: registros de entrada/saida - ordem de chegada dos veiculos
    // arraylist mantem ordem de inserção e tem acesso O(1) por indice, ideal p/ logs
    private ArrayList<Registro> registros;

    /* c03: linkedlist<mensalista> - escolha justificada:
     *   cadastro/remocao de mensalistas ocorre nas pontas (add/remove) -> O(1)
     *   linkedlist e lista duplamente encadeada: sem deslocamento de elementos
     *   arraylist exigiria deslocamento O(n) para remover mensalista do meio, ineficiente
     *   mensalistas nao precisam de acesso aleatorio por indice, iteracao sequencial é suficiente
     */
    private LinkedList<Mensalista> mensalistas;

    public GerenciadorEstacionamento() {
        vagas = new HashMap<>(); 
        registros = new ArrayList<>(); //C02 aqui
        mensalistas = new LinkedList<>(); //C03 adicionado agora!!
        inicializarVagas();
    }

    // cria as 30 vagas do estacionamento (A01-A15, B01-B15) e popula o map
    private void inicializarVagas() {
        for (int i = 1; i <= 15; i++) {
            String n = String.format("%02d", i);
            vagas.put("A" + n, new Vaga("A" + n));
            vagas.put("B" + n, new Vaga("B" + n));
        }
    }
    
    //co3: cadastro de mensalista, adicionando a vaga como reservada
    public void cadastrarMensalista(Mensalista m) throws VagaOcupadaException {
        Vaga vaga = vagas.get(m.getIdVagaReservada());
        if (vaga == null || !vaga.isDisponivel()) {
            throw new VagaOcupadaException(m.getIdVagaReservada());
        }
        vaga.setStatus(StatusVaga.RESERVADA);
        mensalistas.add(m); // add nas pontas = O(1) na linkedlist
    }

    // c03: remove mensalista via iterator - evita ConcurrentModificationException
    // iterator.remove() e o unico remove() seguro durante iter. na propria lista
    public void removerMensalista(String placa) {
        Iterator<Mensalista> it = mensalistas.iterator();
        while (it.hasNext()) {
            Mensalista m = it.next();
            if (m.getPlaca().equalsIgnoreCase(placa)) {
                Vaga vaga = vagas.get(m.getIdVagaReservada());
                if (vaga != null) vaga.setStatus(StatusVaga.LIVRE);
                it.remove(); // remove seguro via iterator
                break;
            }
        }
    }

    public boolean isMensalista(String placa) {
        for (Mensalista m : mensalistas)
            if (m.getPlaca().equalsIgnoreCase(placa)) return true;
        return false;
    }


    //evita modificações externas nas structs internas
    public Map<String, Vaga> getVagas() {
        return Collections.unmodifiableMap(vagas);
    }
    // c02 ordem de chegada
    public List<Registro> getRegistros() {
        return Collections.unmodifiableList(registros);
    }
    // c03
    public List<Mensalista> getMensalistas() {
        return Collections.unmodifiableList(mensalistas);
    }
}
