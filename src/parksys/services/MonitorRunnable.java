package parksys.services;
import parksys.entities.Vaga;
import parksys.enums.StatusVaga;

import java.util.List;
import java.util.Map;

/*
 * thread daemon para monitoramento das vagas
 * m06: o monitorrunnable implementa runnable para rodar em paralelo como uma thread daemon.
 * uma thread daemon significa que ela roda em background e a jvm fecha ela sozinha e de forma automatica
 * assim que todas as outras threads principais de usuario (nao-daemon) terminarem de rodar.
 * a regra de ouro para isso funcionar e chamar o metodo thread.setdaemon(true) antes de dar thread.start().
 * se tentar chamar setdaemon depois do start, o java quebra e lanca a excecao illegalthreadstateexception.
 * 
 */

public class MonitorRunnable implements Runnable {private final GerenciadorEstacionamento gerenciador;

    // m06: construtor recebendo o gerenciador por injecao de dependencia para ter acesso ao mapa de vagas
    public MonitorRunnable(GerenciadorEstacionamento gerenciador) {
        this.gerenciador = gerenciador;
    }

    /*
     * m06: o metodo run() executa o laco continuo controlando o tempo com o sleep(1000).
     * o metodo isinterrupted() fica checando se a thread recebeu ordens para parar sem limpar o flag.
     * quando o metodo monitor.interrupt() for chamado por fora, o flag vira true e o loop quebra.
     */
    @Override
    public void run() {
        System.out.println("[monitor] daemon iniciado");
        while (!Thread.currentThread().isInterrupted()) {
            try {
                // m06: pega o mapa imutavel de vagas do gerenciador para fazer a leitura segura
                Map<String, Vaga> vagas = gerenciador.getVagas();

                // usando streams do java para filtrar e contar de forma rapida as vagas por cada status
                long livres    = vagas.values().stream().filter(v -> v.getStatus() == StatusVaga.LIVRE).count();
                long ocupadas  = vagas.values().stream().filter(v -> v.getStatus() == StatusVaga.OCUPADA).count();
                long reservadas= vagas.values().stream().filter(v -> v.getStatus() == StatusVaga.RESERVADA).count();

                // exibe o relatorio formatado no console mostrando o status em tempo real do patio
                System.out.printf("[monitor] livres: %2d | ocupadas: %2d | reservadas: %2d%n",
                        livres, ocupadas, reservadas);

                // m06: segura a thread por 1000 milissegundos para gerar exatamente 1 relatorio por segundo
                Thread.sleep(1000); 

            } catch (InterruptedException e) {
                // m06: se a thread for interrompida enquanto estava dormindo no sleep, captura a excecao.
                // o protocolo obrigatorio e restaurar o flag chamando o interrupt() de novo manualmente
                // para garantir que a thread encerre o loop de forma limpa e segura.
                Thread.currentThread().interrupt();
                System.out.println("[monitor] daemon encerrado");
            }
        }
    }


}
