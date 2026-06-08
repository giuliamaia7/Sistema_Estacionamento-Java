package parksys.main;
import parksys.entities.Registro;
import parksys.enums.TipoVeiculo;
import parksys.services.EntradaRunnable;
import parksys.services.GerenciadorEstacionamento;
import parksys.services.MonitorRunnable;
import parksys.ui.TelaInicial;

import javax.swing.*;
import java.util.List;

// ponto de entrada principal do sistema parksys
public class Principal{public static void main(String[] args) {

        System.out.println("=== ParkSys — Demonstração de Multithreading ===");

        // m01: busca a unica instancia do gerenciador usando o padrao singleton
        GerenciadorEstacionamento gerenciador = GerenciadorEstacionamento.getInstance();

        // m06: cria a thread de monitoramento em tempo real baseada no monitorrunnable
        // setamos o setdaemon(true) obrigatoriamente antes do start() para avisar a jvm que ela 
        // e uma thread de background (daemon) e deve fechar sozinha quando o app principal morrer
        Thread monitor = new Thread(new MonitorRunnable(gerenciador), "Monitor-Daemon");
        monitor.setDaemon(true);
        monitor.start();

        // m01: cria o array de threads contendo o minimo de 4 threads exigido pelo enunciado
        // cada uma dessas threads simula a chegada concorrente de um veiculo diferente na cancela
        Thread[] threads = {
            new Thread(new EntradaRunnable("ABC1234", TipoVeiculo.CARRO,"A01", gerenciador), "Entrada-1"),
            new Thread(new EntradaRunnable("XYZ5678", TipoVeiculo.MOTO,"A03", gerenciador), "Entrada-2"),
            new Thread(new EntradaRunnable("DEF9012", TipoVeiculo.SUV,"A04", gerenciador), "Entrada-3"),
            new Thread(new EntradaRunnable("GHI3456", TipoVeiculo.CAMINHAO, "B01", gerenciador), "Entrada-4"),
            new Thread(new EntradaRunnable("JKL7890", TipoVeiculo.CARRO, "A06", gerenciador), "Entrada-5"),
        };

        // m02: o laco percorre o array disparando o metodo start() em cada thread
        // isso joga elas no estado de prontas para rodar em paralelo de verdade pela jvm
        System.out.println("[main] Iniciando " + threads.length + " threads...");
        for (Thread t : threads) {
            t.start();
        }

        // m02: usamos o join() para obrigar a thread principal (main) a esperar sentada ate que 
        // cada uma das threads de entrada termine de executar antes de continuar o fluxo do codigo.
        // envolvemos em um try-catch obrigatorio caso a thread main seja interrompida no meio dessa espera.
        for (Thread t : threads) {
            try {
                t.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.err.println("[main] Interrompido no join: " + t.getName());
            }
        }

        System.out.println("[main] Todas as threads concluídas.");

        // m04 / m07: exibe o relatorio provando que o campo transient guardou o nome da thread de origem.
        // como os dados foram inseridos agora em memoria (e nao vindos do .ser), o campo threadorigem nao fica nulo.
        System.out.println("\n=== Thread de origem dos registros ===");
        List<Registro> registros = gerenciador.getRegistros();
        for (Registro r : registros) {
            System.out.printf("  Placa: %-8s | Thread: %s%n", r.getPlaca(), r.getThreadOrigem());
        }
        System.out.println("[M07] Se carregados de arquivo, a coluna Thread mostraria null (campo transient).");

        // interrompe o monitor daemon de forma limpa mudando o flag com o interrupt
        monitor.interrupt();

        // abre a tela grafica principal do sistema respeitando a thread correta de eventos do swing (edt)
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                } catch (Exception e) {
                    // se falhar o visual do sistema operacional, mantem a aparencia padrao do java swing
                }
                TelaInicial tela = new TelaInicial();
                tela.setVisible(true);
            }
        });
    }

}
