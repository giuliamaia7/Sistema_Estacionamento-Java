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

*Em desenvolvimento.*

## Requisitos Funcionais

O sistema possui as seguintes funcionalidades:

- Registro de entrada e saída de veículos;
- Gerenciamento de vagas com diferentes tamanhos;
- Cadastro de mensalistas;
- Cálculo automático de tarifas por tipo de veículo;
- Emissão de relatórios.