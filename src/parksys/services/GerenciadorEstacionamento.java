package parksys.services;

import parksys.entities.Mensalista;
import parksys.entities.Registro;
import parksys.entities.Vaga;
import parksys.enums.StatusVaga;
import parksys.exceptions.VagaOcupadaException;
import parksys.exceptions.VeiculoNaoEncontradoException;
import parksys.enums.TipoVeiculo;
import parksys.exceptions.PlacaInvalidaException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Comparator;
import java.util.stream.Collectors;
import java.util.TreeSet;


public class GerenciadorEstacionamento {

    // regex p/ validar placa: ABC1234 (padrao) ou ABC1D23 (mercosul)
    private static final String REGEX_PLACA = "^[A-Z]{3}\\d{4}$|^[A-Z]{3}\\d[A-Z]\\d{2}$";



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
        mensalistas = new LinkedList<>(); //C03 
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

    /*
        T03: calcula valor pago com base no tempo de permanencia e tipo do veiculo
        Usando getTarifaHora() do enum TipoVeiculo, sem hard-code de valores
        O calculo arredonda para cima e cobra no minimo 1 hora
        ou seja 1h e 10min ->  cobra 2h, 0h e 30min -> cobra 1h
    */
    /*
        T04: registra entrada alocando vagas consecutivas conforme tipo do veiculo
        Usa getVagasOcupadas() do enum TipoVeiculo p/ saber quantas vagas alocar
         - Carro: 1 vaga, Moto: 1 vaga, Caminhao: 3 vagas
        Verifica disponibilidade de todas as vagas necessárias antes de alocar (fail-fast)
         - evita alocar parcialmente e precisar desfazer se faltar vagas no meio
    */
    public void registrarEntrada(String placa, TipoVeiculo tipo, String idVagaInicial)
            throws VagaOcupadaException, PlacaInvalidaException {

        validarPlaca(placa);

        // t04: qtd vagas = dado de neg. do enum, nao magic number no cod.
        int  qtd    = tipo.getVagasOcupadas();
        char fileira = Character.toUpperCase(idVagaInicial.charAt(0));
        int  num     = Integer.parseInt(idVagaInicial.substring(1));

        // verifica disponibilidade de TODAS as vagas antes de alocar qualquer uma
        // fail - fast evita alocar parcialmente e precisar desfazer
        List<String> ids = new ArrayList<>(qtd);
        for (int i = 0; i < qtd; i++) {
            String id  = fileira + String.format("%02d", num + i);
            Vaga   vag = vagas.get(id);
            if (vag == null || !vag.isDisponivel()) throw new VagaOcupadaException(id);
            ids.add(id);
        }

        // aloca todas as vagas verificadas acima
        for (String id : ids) vagas.get(id).setStatus(StatusVaga.OCUPADA);

        // c02: add() ao final do arraylist - O(1) amortizado
        Registro reg = new Registro(placa.toUpperCase(), tipo, ids, LocalDateTime.now());
        registros.add(reg);
    }

    // t03: calcula valor usando getTarifaHora() do enum - ver commit T03
    public Registro registrarSaida(String placa) throws VeiculoNaoEncontradoException {
        // c02: busca linear no arraylist p/ achar reg. ativo da placa
        Registro regAtivo = null;
        for (Registro r : registros) {
            if (r.getPlaca().equalsIgnoreCase(placa) && r.getDataSaida() == null) {
                regAtivo = r;
                break;
            }
        }
        if (regAtivo == null) throw new VeiculoNaoEncontradoException(placa);

        LocalDateTime saida = LocalDateTime.now();
        regAtivo.setDataSaida(saida);

        // t03: getTarifaHora() encapsula o preco no enum - sem "5.0", "10.0" hard-coded
        long   min   = ChronoUnit.MINUTES.between(regAtivo.getDataEntrada(), saida);
        double horas = Math.max(1.0, Math.ceil(min / 60.0));
        regAtivo.setValorPago(horas * regAtivo.getTipoVeiculo().getTarifaHora());

        // libera todas as vagas ocupadas pelo veiculo (1, 2 ou 3 dependendo do tipo)
        for (String id : regAtivo.getIdsVagas()) {
            Vaga v = vagas.get(id);
            if (v != null) v.setStatus(StatusVaga.LIVRE);
        }
        return regAtivo;
    }
    
    // validacao de placa via regex - lanca exc. checada p/ forcar tratamento
    private void validarPlaca(String placa) throws PlacaInvalidaException {
        if (placa == null || placa.isBlank() || !placa.toUpperCase().matches(REGEX_PLACA))
            throw new PlacaInvalidaException(placa);
    }

    /*c04: TreeSet organiza os registros de forma automatica usando a ordem natural
    Usa o compareTo() criado na classe Registro (ordem cronológica de entrada)
    Evita duplicatas e mantem os logs sempre ordenados por data sem usar sort() 
    */
    public TreeSet<Registro> getRegistrosOrdenados() {
        return new TreeSet<>(registros);
    }

    /* c05: Comparator define uma ordenação alternativa (por faturamento descrescente)
     * diferença: Comparable é a ordem natural interna e fixa da classe
     * Comparator é uma regra externa, permitindo criar ordenações variadas
     * filtramos os registros finalizados e ordenamos do maior valor para o menor
     */
    public List<Registro> getRegistrosPorReceita() {
        return registros.stream()
                .filter(r -> r.getDataSaida() != null)
                .sorted(Comparator.comparingDouble(Registro::getValorPago).reversed())
                .collect(Collectors.toList());
    }

    // c06: calcula a receita total acumulada do estacionamento
    // Percorre a lista e soma o valorPago apenas dos veículos que já saíram (finalizados)
    public double calcularReceita() {
        double total = 0;
        for (Registro r : registros) {
            if (r.getDataSaida() != null) {
                total += r.getValorPago();
            }
        }
        return total;
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

    public List<Vaga> getVagasDisponiveis() {
        List<Vaga> livres = new ArrayList<>();
        for (Vaga v : vagas.values()) if (v.isDisponivel()) livres.add(v);
        livres.sort(Comparator.comparing(Vaga::getId));
        return livres;
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

    public void restaurarDados(Map<String, Vaga> v, List<Registro> r, List<Mensalista> m) {
        if (v != null && !v.isEmpty()) vagas       = new HashMap<>(v);
        if (r != null && !r.isEmpty()) registros   = new ArrayList<>(r);
        if (m != null && !m.isEmpty()) mensalistas  = new LinkedList<>(m);
    }

}
