package parksys.services;

import parksys.enums.TipoVeiculo;
import parksys.exceptions.PlacaInvalidaException;
import parksys.exceptions.VagaOcupadaException;

/*
 * classe yara silva - tarefa de entrada rodando em thread separada
 * m01: implementamos runnable em vez de estender a classe thread. isso e uma boa pratica
 * porque o java nao deixa fazer heranca multipla. se a gente usasse extends thread, nao daria
 * para estender mais nenhuma outra classe. alem disso, separa o que a tarefa faz do mecanismo que roda ela.
 * a jvm vai chamar o metodo run() de forma automatica assim que o thread.start() for disparado.
 */
public class EntradaRunnable implements Runnable {
// m01: os 4 parametros que o enunciado do projeto exige para identificar a entrada
    private final String                    placa;
    private final TipoVeiculo               tipo;
    private final String                    idVagaDesejada;
    private final GerenciadorEstacionamento gerenciador;

    // m01: contrutor fazendo a injecao de dependencia. a thread ja nasce recebendo tudo o que precisa
    // para trabalhar, o que evita ficar usando chamadas estaticas soltas pelo codigo.
    public EntradaRunnable(String placa, TipoVeiculo tipo,
                           String idVagaDesejada,
                           GerenciadorEstacionamento gerenciador) {
        this.placa          = placa;
        this.tipo           = tipo;
        this.idVagaDesejada = idVagaDesejada;
        this.gerenciador    = gerenciador;
    }

    /*
     * m02: o metodo run() e o coracao da thread e roda em paralelo quando damos new thread(this).start()
     * usamos o thread.sleep(200) para simular o tempo de resposta do mundo real, como a camera lendo a placa.
     * * tratamento de interrupcao: o sleep() lanca interruptedException se a thread for cortada no meio.
     * quando isso acontece, o java limpa o sinal de interrupcao por seguranca. por isso, a regra obrigatoria e:
     * 1) capturar a excecao no catch.
     * 2) chamar thread.currentthread().interrupt() para setar o flag de interrupcao de novo de forma manual.
     * sem esse reset, o sistema operacional ou outras partes do software nao saberiam que a thread foi parada.
     */
    @Override
    public void run() {
        try {
            // m02: bota a thread para dormir por 200 milissegundos simulando o processamento da cancela
            Thread.sleep(200); 

            // m03: chama o registrarentrada que e synchronized la no gerenciador para evitar que duas threads
            // tentem enfiar um carro na mesma vaga ao mesmo tempo (evita condicao de corrida)
            gerenciador.registrarEntrada(placa, tipo, idVagaDesejada);

            System.out.printf("[%s] entrada ok: %s -> %s%n",
                    Thread.currentThread().getName(), placa, idVagaDesejada);

        } catch (InterruptedException e) {
            // m02: limpa o estado de forma correta e avisa o sistema reativando o flag de interrupcao
            Thread.currentThread().interrupt();
            System.err.printf("[%s] interrompida durante reg. de %s%n",
                    Thread.currentThread().getName(), placa);

        } catch (VagaOcupadaException e) {
            // trata o erro caso a thread tente alocar uma vaga que ja foi preenchida em paralelo
            System.err.printf("[%s] vaga ocupada: %s%n",
                    Thread.currentThread().getName(), e.getMessage());

        } catch (PlacaInvalidaException e) {
            // captura o erro se o formato da placa enviado por essa thread estiver fora do padrao da regex
            System.err.printf("[%s] placa invalida: %s%n",
                    Thread.currentThread().getName(), e.getMessage());
        }
        // quando o run() chega ao fim, a thread morre e limpa a memoria sozinha, sem usar funcoes perigosas como thread.stop()
    }

}
