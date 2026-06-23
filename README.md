# ParkSys

## Descrição

Sistema de gerenciamento de estacionamento de veículos com interface gráfica Swing.

Projeto desenvolvido como atividade avaliativa da disciplina de Programação Orientada a Objetos, ministrada pelo professor Júnior Fernandes Marques.

## Integrantes

- Giulia
- Natália
- Yara

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

---

## Requisitos Funcionais

O sistema possui as seguintes funcionalidades:

- Registro de entrada e saída de veículos;
- Gerenciamento de vagas com diferentes tamanhos;
- Cadastro de mensalistas;
- Cálculo automático de tarifas por tipo de veículo;
- Emissão de relatórios.