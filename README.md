# ParkSys

## Descrição

Sistema de gerenciamento de estacionamento de veículos com interface gráfica Swing.

Projeto desenvolvido como atividade avaliativa da disciplina de Programação Orientada a Objetos, ministrada pelo professor Júnior Fernandes Marques.

## Integrantes

- Giulia
- Natália
- Yara


# Tecnologias
```
| Tecnologia            | Finalidade                |
| --------------------- | ------------------------- |
| Java 17               | Linguagem principal       |
| Java Swing            | Interface gráfica         |
| Collections Framework | Estruturas de dados       |
| Serialização Java     | Persistência dos dados    |
| Threads               | Processamento concorrente |
| Git/GitHub            | Versionamento do projeto  |

```

## Estrutura do Projeto

```text
src/
└── parksys/
    ├── main/
    │   └── Principal.java
    │
    ├── entities/
    │   ├── Vaga.java
    │   ├── Veiculo.java
    │   ├── Registro.java
    │   └── Mensalista.java
    │
    ├── enums/
    │   ├── TipoVeiculo.java
    │   └── StatusVaga.java
    │
    ├── exceptions/
    │   ├── VagaOcupadaException.java
    │   ├── VeiculoNaoEncontradoException.java
    │   └── PlacaInvalidaException.java
    │
    ├── services/
    │   ├── GerenciadorEstacionamento.java
    │   ├── GerenciadorArquivo.java
    │   ├── EntradaRunnable.java
    │   └── MonitorRunnable.java
    │
    ├── observer/
    │   ├── EstacionamentoObserver.java
    │   └── PainelMonitor.java
    │
    └── ui/
        ├── TelaInicial.java
        ├── TelaCadastroMensalista.java
        ├── TelaRegistroEntrada.java
        ├── TelaSaida.java
        └── TelaRelatorio.java
```

## Como Executar

### Pré-requisitos

- JDK 11 ou superior instalado
- Arquivo `fundo.jpg` no classpath (pasta raiz do projeto ou `src/`) para a imagem de fundo da tela inicial

### Via linha de comando

```bash
# 1. Compile todos os arquivos .java
javac -d out $(find parksys -name "*.java")

# 2. Execute a classe principal
java -cp out parksys.main.Principal
```

### Via IDE (IntelliJ / Eclipse / VS Code)

1. Importe o projeto como **Java Project** apontando para a pasta raiz
2. Defina `parksys.main.Principal` como classe principal
3. Execute com `Run` ou `F5`

### Comportamento ao iniciar

Ao executar, o sistema:

1. Imprime uma demonstração de multithreading no console
2. Exibe o relatório de threads de origem dos registros
3. Abre a interface gráfica Swing automaticamente



# Branches
```
| Branch               | Descrição                   |
| -------------------- | --------------------------- |
| main                 | Versão principal do projeto |
| feature/enums        | Implementação dos enums     |
| feature/entities     | Classes de domínio          |
| feature/services     | Regras de negócio           |
| feature/threads      | Recursos de concorrência    |
| feature/patterns     | Padrões de projeto          |
```


## Requisitos Funcionais

O sistema possui as seguintes funcionalidades:

- Registro de entrada e saída de veículos;
- Gerenciamento de vagas com diferentes tamanhos;
- Cadastro de mensalistas;
- Cálculo automático de tarifas por tipo de veículo;
- Emissão de relatórios.

## Conceitos Aplicados

### Padrão Singleton

O `GerenciadorEstacionamento` garante que apenas uma instância exista em toda a aplicação. O método `getInstance()` é `synchronized` para ser seguro em ambiente multithreaded.

```java
// GerenciadorEstacionamento.java
private static GerenciadorEstacionamento instancia;

public static synchronized GerenciadorEstacionamento getInstance() {
    if (instancia == null) {
        instancia = new GerenciadorEstacionamento();
    }
    return instancia;
}
```

Todas as telas acessam o mesmo gerenciador via `getInstance()`, sem precisar passar referências entre janelas.

---

### Padrão Observer

A interface `EstacionamentoObserver` desacopla o `GerenciadorEstacionamento` das telas que precisam reagir a mudanças de vagas. O gerenciador notifica todos os observadores registrados sempre que uma vaga muda de status.

```java
// EstacionamentoObserver.java
public interface EstacionamentoObserver {
    void onVagaAlterada(String idVaga, StatusVaga novoStatus);
}

// GerenciadorEstacionamento.java — notifica todos os observers
private void notificarObservadores(String idVaga, StatusVaga novoStatus) {
    for (EstacionamentoObserver obs : observadores) {
        obs.onVagaAlterada(idVaga, novoStatus);
    }
}
```

O `PainelMonitor` implementa a interface e mantém um mapa local com o status mais recente de cada vaga, podendo contar por status com Streams:

```java
// PainelMonitor.java
public long contarPorStatus(StatusVaga status) {
    return mapaStatus.values().stream()
            .filter(s -> s == status)
            .count();
}
```

---

### Padrão MVC (separação de responsabilidades)

As telas Swing não contêm lógica de negócio — apenas delegam ao `GerenciadorEstacionamento`. Isso mantém a camada de apresentação isolada das regras do domínio.

```java
// TelaRegistroEntrada.java — a tela só chama o gerenciador
private void registrar() {
    String placa     = txtPlaca.getText().trim().toUpperCase();
    TipoVeiculo tipo = (TipoVeiculo) cmbTipo.getSelectedItem();
    String idVaga    = vagaItem.split("\\s+")[0];
    try {
        gerenciador.registrarEntrada(placa, tipo, idVaga); // toda lógica fica aqui
        JOptionPane.showMessageDialog(this, "Entrada registrada! ...");
    } catch (VagaOcupadaException | PlacaInvalidaException ex) {
        erro(ex.getMessage());
    }
}
```

---

### Multithreading — Runnable e Thread Daemon

O sistema usa `Runnable` (em vez de `extends Thread`) para separar a tarefa do mecanismo de execução. O `EntradaRunnable` simula a chegada concorrente de veículos na cancela:

```java
// Principal.java — 5 threads disparadas em paralelo
Thread[] threads = {
    new Thread(new EntradaRunnable("ABC1234", TipoVeiculo.CARRO, "A01", gerenciador), "Entrada-1"),
    new Thread(new EntradaRunnable("XYZ5678", TipoVeiculo.MOTO,  "A03", gerenciador), "Entrada-2"),
    // ...
};
for (Thread t : threads) t.start();
for (Thread t : threads) t.join(); // aguarda todas terminarem
```

O `MonitorRunnable` roda como **thread daemon** — a JVM a encerra automaticamente quando as threads principais terminam:

```java
// Principal.java
Thread monitor = new Thread(new MonitorRunnable(gerenciador), "Monitor-Daemon");
monitor.setDaemon(true); // obrigatório antes do start()
monitor.start();
```

---

### Sincronização — `synchronized`

Todos os métodos do `GerenciadorEstacionamento` que acessam ou modificam as coleções compartilhadas são `synchronized`, evitando condições de corrida:

```java
// GerenciadorEstacionamento.java
public synchronized void registrarEntrada(String placa, TipoVeiculo tipo, String idVagaInicial)
        throws VagaOcupadaException, PlacaInvalidaException {
    validarPlaca(placa);
    // verifica e aloca vagas de forma atômica — nenhuma outra thread entra aqui ao mesmo tempo
    for (String id : ids) {
        vagas.get(id).setStatus(StatusVaga.OCUPADA);
        notificarObservadores(id, StatusVaga.OCUPADA);
    }
}
```

---

### Estruturas de Dados — Coleções justificadas

Cada coleção foi escolhida com base na operação predominante:

**HashMap** para vagas — lookup O(1) por ID:
```java
private HashMap<String, Vaga> vagas; // busca "A01" instantânea
```

**ArrayList** para registros — inserção O(1) amortizado e acesso por índice:
```java
private ArrayList<Registro> registros; // mantém ordem de chegada
```

**LinkedList** para mensalistas — inserção e remoção nas pontas O(1):
```java
private LinkedList<Mensalista> mensalistas; // add/remove nas pontas sem deslocamento
```

**TreeSet** para relatório cronológico — mantém elementos ordenados automaticamente via `Comparable`:
```java
// Registro.java — define a ordem natural
@Override
public int compareTo(Registro outro) {
    int resultado = this.dataEntrada.compareTo(outro.dataEntrada);
    if (resultado != 0) return resultado;
    return this.placa.compareTo(outro.placa); // desempate pela placa
}

// GerenciadorEstacionamento.java — TreeSet garante ordem sem sort()
public synchronized TreeSet<Registro> getRegistrosOrdenados() {
    return new TreeSet<>(registros);
}
```

**Comparator externo** para ordenar por receita (ordem alternativa, sem modificar a classe):
```java
public synchronized List<Registro> getRegistrosPorReceita() {
    return registros.stream()
            .filter(r -> r.getDataSaida() != null)
            .sorted(Comparator.comparingDouble(Registro::getValorPago).reversed())
            .collect(Collectors.toList());
}
```

---

### Serialização Java

Os dados são persistidos em um único arquivo binário `.ser` usando o value object `DadosParkSys`, que agrupa as três coleções:

```java
// GerenciadorArquivo.java — serialização com try-catch-finally explícito
public static void serializar(Map<String, Vaga> vagas,
                              List<Registro>    registros,
                              List<Mensalista>  mensalistas,
                              String path) {
    ObjectOutputStream oos = null;
    try {
        oos = new ObjectOutputStream(new FileOutputStream(path));
        oos.writeObject(new DadosParkSys(new HashMap<>(vagas), ...));
    } catch (IOException e) {
        System.err.println("[arq] erro ao serializar: " + e.getMessage());
    } finally {
        fechar(oos); // fecha o stream de qualquer forma
    }
}
```

O campo `threadOrigem` em `Registro` é marcado como `transient` — não é gravado no arquivo. Isso demonstra que, ao carregar dados de um `.ser`, esse campo retorna `null`:

```java
// Registro.java
private transient String threadOrigem; // ignorado pelo ObjectOutputStream
```

---

### Exceções Checadas de Domínio

O sistema usa exceções checadas para forçar o tratamento de erros de negócio em tempo de compilação:

```java
// PlacaInvalidaException.java
public PlacaInvalidaException(String placa) {
    super("Placa inválida: \"" + placa + "\". Use ABC1234 (padrão) ou ABC1D23 (Mercosul).");
}

// GerenciadorEstacionamento.java — validação por regex
private static final String REGEX_PLACA = "^[A-Z]{3}\\d{4}$|^[A-Z]{3}\\d[A-Z]\\d{2}$";

private void validarPlaca(String placa) throws PlacaInvalidaException {
    if (placa == null || placa.isBlank() || !placa.toUpperCase().matches(REGEX_PLACA))
        throw new PlacaInvalidaException(placa);
}
```

---

### Enums com comportamento

Os enums não são apenas constantes — carregam dados e comportamento encapsulados:

```java
// TipoVeiculo.java
public enum TipoVeiculo {
    MOTO    ("Motocicleta",      5.00, 1),
    CARRO   ("Automóvel",       10.00, 1),
    SUV     ("Caminhonete/SUV", 18.00, 2),
    CAMINHAO("Caminhão",        30.00, 3);

    private final String nomeExibicao;
    private final double tarifaHora;
    private final int    vagasOcupadas;
    // ... getters
}
```

A tarifa é calculada dinamicamente usando `getTarifaHora()`, sem valores fixos no código:

```java
// GerenciadorEstacionamento.java
double horas = Math.max(1.0, Math.ceil(min / 60.0));
regAtivo.setValorPago(horas * regAtivo.getTipoVeiculo().getTarifaHora());
```

---

