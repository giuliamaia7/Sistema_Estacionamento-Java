package parksys.services;

import parksys.entities.Mensalista;
import parksys.entities.Registro;
import parksys.entities.Vaga;
import parksys.enums.StatusVaga;
import parksys.exceptions.VagaOcupadaException;
import parksys.exceptions.VeiculoNaoEncontradoException;
import parksys.observer.EstacionamentoObserver;
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


public class GerenciadorEstacionamento {// regex p/ validar placa: ABC1234 (padrao) ou ABC1D23 (mercosul)
    private static final String REGEX_PLACA = "^[A-Z]{3}\\d{4}$|^[A-Z]{3}\\d[A-Z]\\d{2}$";



    // p01: referencia estatica que guarda a instancia unica do singleton na memoria
    private static GerenciadorEstacionamento instancia;

    // c01: hashmap<string,vaga> - lookup O(1) p/ id da vaga
    // chave = cod  (ex: "A01"), val = obj Vaga c/ status atual
    // arraylist exigiria iter. linear O(n) p/ achar vaga por id - inaceitavel
    private HashMap<String, Vaga> vagas;
    // c02: registros de entrada/saida - ordem de chegada dos veiculos
    // arraylist mantem ordem de inserção e tem acesso O(1) por indice, ideal p/ logs
    private ArrayList<Registro> registros;

    /* c03: linkedlist<mensalista> - escolha justificada:
     * cadastro/remocao de mensalistas ocorre nas pontas (add/remove) -> O(1)
     * linkedlist e lista duplamente encadeada: sem deslocamento de elementos
     * arraylist exigiria deslocamento O(n) para remover mensalista do meio, ineficiente
     * mensalistas nao precisam de acesso aleatorio por indice, iteracao sequencial é suficiente
     */
    private LinkedList<Mensalista> mensalistas;

    // p03: lista de observadores cadastrados esperando atualizacao sobre a vaga
    private List<EstacionamentoObserver> observadores;

    /* p01: construtor privado impede uso do new fora daqui e trava objeto unico.
     * garante que toda a aplicacao use a mesma estrutura de dados e colecoes.
     */
    private GerenciadorEstacionamento() {
        vagas = new HashMap<>(); 
        registros = new ArrayList<>(); //C02 aqui
        mensalistas = new LinkedList<>(); //C03 
        observadores = new ArrayList<>(); // p03: inicializa a colecao de ouvintes
        inicializarVagas();
    }

    /* p01: pega a instancia compartilhada do gerenciador.
     * synchronized na assinatura evita criacao duplicada por threads simultaneas.
     */
    public static synchronized GerenciadorEstacionamento getInstance() {
        if (instancia == null) {
            instancia = new GerenciadorEstacionamento();
        }
        return instancia;
    }

    // cria as 30 vagas do estacionamento (A01-A15, B01-B15) e popula o map
    private void inicializarVagas() {
        for (int i = 1; i <= 15; i++) {
            String n = String.format("%02d", i);
            vagas.put("A" + n, new Vaga("A" + n));
            vagas.put("B" + n, new Vaga("B" + n));
        }
    }

    /* p03: adiciona um novo observador na lista de transmissao.
     * p06: chamado ao iniciar o monitor na tela do sistema.
     */
    public synchronized void addObserver(EstacionamentoObserver obs) {
        if (!observadores.contains(obs)) {
            observadores.add(obs);
        }
    }

    /* p03: remove o observador cadastrado na lista.
     * p06: chamado ao fechar a janela para liberar recurso da interface.
     */
    public synchronized void removeObserver(EstacionamentoObserver obs) {
        observadores.remove(obs);
    }

    // p03: varre a lista de observers cadastrados e dispara o aviso de mudanca de status
    private void notificarObservadores(String idVaga, StatusVaga novoStatus) {
        for (EstacionamentoObserver obs : observadores) {
            obs.onVagaAlterada(idVaga, novoStatus);
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
    /*
        m03: adicionado synchronized para travar o metodo de entrada. como temos varias threads
        rodando juntas no sistema, se duas tentarem dar entrada na mesma vaga no mesmo instante,
        aconteceria uma condicao de corrida (race condition) e o mapa ficaria todo inconsistente.
        com o synchronized(this), a thread pega o lock do gerenciador e bloqueia as outras ate terminar.
    */
    public synchronized void registrarEntrada(String placa, TipoVeiculo tipo, String idVagaInicial)
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

        // aloca todas as vagas verificadas acima e atualiza o observer
        for (String id : ids) {
            vagas.get(id).setStatus(StatusVaga.OCUPADA);
            notificarObservadores(id, StatusVaga.OCUPADA); // p03: atualiza o estado visual da vaga no painel
        }

        // c02: add() ao final do arraylist - O(1) amortizado
        Registro reg = new Registro(placa.toUpperCase(), tipo, ids, LocalDateTime.now());
        
        /* m04: pegamos o nome da thread que esta executando o registro no momento (ex: "entrada-1")
            e guardamos dentro do objeto registro. essa variavel foi marcada como transient na classe,
            entao ela vai ficar nula quando o arquivo for desserializado, servindo so para o log em tempo real.
        */
        reg.setThreadOrigem(Thread.currentThread().getName());

        registros.add(reg);
    }

    // t03: calcula valor usando getTarifaHora() do enum - ver commit T03
    // m03: synchronized na saida porque mexe na leitura e modificacao do arraylist e do hashmap de vagas compartilhados
    public synchronized Registro registrarSaida(String placa) throws VeiculoNaoEncontradoException {
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

        // libera todas as vagas ocupadas pelo veiculo e avisa o monitor cadastrado
        for (String id : regAtivo.getIdsVagas()) {
            Vaga v = vagas.get(id);
            if (v != null) {
                v.setStatus(StatusVaga.LIVRE);
                notificarObservadores(id, StatusVaga.LIVRE); // p03: limpa a cor da vaga na interface
            }
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
    // m03: sincronizado para evitar ler os registros enquanto outra thread adiciona ou altera dados
    public synchronized TreeSet<Registro> getRegistrosOrdenados() {
        return new TreeSet<>(registros);
    }

    /* c05: Comparator define uma ordenação alternativa (por faturamento descrescente)
     * diferença: Comparable é a ordem natural interna e fixa da classe
     * Comparator é uma regra externa, permitindo criar ordenações variadas
     * filtramos os registros finalizados e ordenamos do maior valor para o menor
     */
    // m03: synchronized para travar o fluxo do stream enquanto os dados do patio estao mudando
    public synchronized List<Registro> getRegistrosPorReceita() {
        return registros.stream()
                .filter(r -> r.getDataSaida() != null)
                .sorted(Comparator.comparingDouble(Registro::getValorPago).reversed())
                .collect(Collectors.toList());
    }

    // c06: calcula a receita total acumulada do estacionamento
    // Percorre a lista e soma o valorPago apenas dos veículos que já saíram (finalizados)
    // m03: travado com synchronized para ler a lista compartilhada de forma segura
    public synchronized double calcularReceita() {
        double total = 0;
        for (Registro r : registros) {
            if (r.getDataSaida() != null) {
                total += r.getValorPago();
            }
        }
        return total;
    }

    
    //co3: cadastro de mensalista, adicionando a vaga como reservada
    // m03: os cadastros mudam as colecoes principais, entao o synchronized garante seguranca entre threads
    public synchronized void cadastrarMensalista(Mensalista m) throws VagaOcupadaException {
        Vaga vaga = vagas.get(m.getIdVagaReservada());
        if (vaga == null || !vaga.isDisponivel()) {
            throw new VagaOcupadaException(m.getIdVagaReservada());
        }
        vaga.setStatus(StatusVaga.RESERVADA);
        mensalistas.add(m); // add nas pontas = O(1) na linkedlist
        notificarObservadores(m.getIdVagaReservada(), StatusVaga.RESERVADA); // p03: muda o mapa de cor da reserva
    }

    // c03: remove mensalista via iterator - evita ConcurrentModificationException
    // iterator.remove() e o unico remove() seguro durante iter. na propria lista
    // m03: adicionado synchronized para ninguem alterar a lista enquanto o iterator estiver rodando
    public synchronized void removerMensalista(String placa) {
        Iterator<Mensalista> it = mensalistas.iterator();
        while (it.hasNext()) {
            Mensalista m = it.next();
            if (m.getPlaca().equalsIgnoreCase(placa)) {
                Vaga vaga = vagas.get(m.getIdVagaReservada());
                if (vaga != null) {
                    vaga.setStatus(StatusVaga.LIVRE);
                    notificarObservadores(m.getIdVagaReservada(), StatusVaga.LIVRE); // p03: libera a vaga visualmente
                }
                it.remove(); // remove seguro via iterator
                break;
            }
        }
    }

    // m03: busca sincronizada para evitar leituras fantasmas enquanto mensalistas sao removidos ou adicionados
    public synchronized boolean isMensalista(String placa) {
        for (Mensalista m : mensalistas)
            if (m.getPlaca().equalsIgnoreCase(placa)) return true;
        return false;
    }

    // m03: o monitor de vagas vai ficar chamando esse metodo toda hora, por isso precisa de sincronismo total
    public synchronized List<Vaga> getVagasDisponiveis() {
        List<Vaga> livres = new ArrayList<>();
        for (Vaga v : vagas.values()) if (v.isDisponivel()) livres.add(v);
        livres.sort(Comparator.comparing(Vaga::getId));
        return livres;
    }

    //evita modificações externas nas structs internas
    // m03: getters sincronizados para evitar ler dados corrompidos enquanto as colecoes estao sendo reescritas
    public synchronized Map<String, Vaga> getVagas() {
        return Collections.unmodifiableMap(vagas);
    }
    // c02 ordem de chegada
    public synchronized List<Registro> getRegistros() {
        return Collections.unmodifiableList(registros);
    }
    // c03
    public synchronized List<Mensalista> getMensalistas() {
        return Collections.unmodifiableList(mensalistas);
    }

    // m03: a restauracao de dados da inicializacao substitui as colecoes, travar e indispensavel
    public synchronized void restaurarDados(Map<String, Vaga> v, List<Registro> r, List<Mensalista> m) {
        if (v != null && !v.isEmpty()) vagas       = new HashMap<>(v);
        if (r != null && !r.isEmpty()) registros   = new ArrayList<>(r);
        if (m != null && !m.isEmpty()) mensalistas  = new LinkedList<>(m);
    }
}
