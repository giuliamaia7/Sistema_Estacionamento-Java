package parksys.services;
import parksys.entities.Mensalista;
import parksys.entities.Registro;
import parksys.entities.Vaga;
import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*responsavel por toda a manipulacao de arquivos do sistema
metodos static para facilitar o acesso sem precisar instanciar a classe
usa serializacao para salvar/ler os dados do estacionamento em um unico arquivo binario
*/

public class GerenciadorArquivo {private static final DateTimeFormatter FMT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    // construtor privado para travar a classe, ja que so usamos funcoes estaticas
    private GerenciadorArquivo() {}

    /*
     * s02: salva as 3 colecoes (vagas, registros e mensalistas) juntas em um arquivo binario
     * fluxo: os dados entram no objeto dadosparksys, passam pelo objectoutputstream e gravam no fileoutputstream
     * usamos try-catch-finally tradicional para fechar o arquivo na marra e nao travar a memoria do linux
     * declaramos as variaveis do arquivo antes do try para conseguir enxergar e fechar elas la no bloco finally
     */
    public static void serializar(Map<String, Vaga> vagas,
                                  List<Registro>    registros,
                                  List<Mensalista>  mensalistas,
                                  String path) {

        // cria copias limpas usando hashmap e arraylist comuns para evitar problemas com listas imutaveis do git
        DadosParkSys dados = new DadosParkSys(
                new HashMap<>(vagas),
                new ArrayList<>(registros),
                new ArrayList<>(mensalistas)
        );

        ObjectOutputStream oos     = null;
        boolean            sucesso = false;
        try {
            // abre o fluxo de escrita apontando para o caminho do arquivo
            oos = new ObjectOutputStream(new FileOutputStream(path));
            oos.writeObject(dados); // grava o pacote completo de objetos de uma vez so no disco
            sucesso = true;
        } catch (IOException e) {
            // s05: captura erro de entrada/saida, tipo falta de espaco em disco ou pasta sem permissao
            System.err.println("[arq] erro ao serializar: " + e.getMessage());
        } finally {
            // s05: o finally roda de qualquer jeito, garantindo o fechamento seguro do arquivo e exibindo o status
            fechar(oos);
            System.out.println("[arq] serializar -> " + (sucesso ? "ok: " + path : "FALHOU"));
        }
    }
    
    // s03: le o arquivo binario do computador e puxa o objeto dadosparksys de volta com tudo dentro
    public static DadosParkSys desserializar(String path) {

        ObjectInputStream ois     = null;
        DadosParkSys      dados   = null;
        boolean           sucesso = false;
        try {
            // abre o fluxo de leitura pegando o arquivo do caminho especificado
            ois    = new ObjectInputStream(new FileInputStream(path));
            dados  = (DadosParkSys) ois.readObject(); // le os bytes e faz o cast obrigatorio para dadosparksys
            sucesso = true;
        } catch (FileNotFoundException e) {
            // s03: cai aqui se for a primeira execucao do sistema, ja que o arquivo ainda nao foi gerado
            System.out.println("[arq] arq. nao encontrado - iniciando c/ dados vazios");
            dados = dadosVazios();
        } catch (IOException e) {
            // s05: trata erros gerais de leitura, tipo arquivo quebrado ou corrompido por fechamento errado
            System.err.println("[arq] ioexception na deser.: " + e.getMessage());
            dados = dadosVazios();
        } catch (ClassNotFoundException e) {
            // s05: erro caso a classe do objeto gravado nao exista mais ou mudou de nome no projeto
            System.err.println("[arq] classe nao encontrada na deser.: " + e.getMessage());
            dados = dadosVazios();
        } finally {
            // s05: garante o fechamento do leitor de arquivos mesmo se der alguma excecao no meio do processo
            fechar(ois);
            System.out.println("[arq] desserializar -> " + (sucesso ? "ok: " + path : "dados vazios"));
        }
        return dados;
    }

    // s04: gera um relatorio em formato texto limpo (csv/txt) para humanos conseguirem ler
    public static void exportarRelatorioTxt(List<Registro> registros, String path) {

        BufferedWriter bw      = null;
        boolean        sucesso = false;
        try {
            // bufferedwriter acumula o texto em memoria antes de cuspir no arquivo, deixando a gravacao muito mais rapida
            bw = new BufferedWriter(new FileWriter(path));

            // desenha o cabecalho obrigatorio do relatorio com a data e hora do processamento atual
            bw.write("========================================"); bw.newLine();
            bw.write("          RELATORIO PARKSYS");             bw.newLine();
            bw.write("  gerado em: " + LocalDateTime.now().format(FMT)); bw.newLine();
            bw.write("========================================"); bw.newLine();
            bw.newLine();

            double totalRec  = 0;
            int    finalizados = 0;
            int    emAndamento = 0;

            // roda a lista pegando as informacoes de cada carro que passou ou esta no patio
            for (Registro r : registros) {
                bw.write("placa   : " + r.getPlaca()); bw.newLine();
                bw.write("tipo    : " + r.getTipoVeiculo().getNomeExibicao()); bw.newLine();
                bw.write("vagas   : " + r.getIdsVagas()); bw.newLine();
                bw.write("entrada : " + r.getDataEntrada().format(FMT)); bw.newLine();

                if (r.getDataSaida() != null) {
                    // se o carro ja saiu, imprime a data de saida e quanto ele pagou na hora
                    bw.write("saida   : " + r.getDataSaida().format(FMT)); bw.newLine();
                    bw.write(String.format("valor   : r$ %.2f", r.getValorPago())); bw.newLine();
                    // m07: exibe o nome da thread que fechou o registro (fica null se veio de arquivo por ser transient)
                    bw.write("thread  : " + r.getThreadOrigem()); bw.newLine();
                    totalRec += r.getValorPago(); // soma no faturamento acumulado
                    finalizados++;
                } else {
                    // se nao tem data de saida, significa que ainda esta ocupando vaga
                    bw.write("saida   : (em andamento)"); bw.newLine();
                    emAndamento++;
                }
                bw.write("----------------------------------------"); bw.newLine();
            }

            // escreve o rodape com a somatoria e contadores exigidos nos requisitos do projeto
            bw.newLine();
            bw.write("finalizados  : " + finalizados); bw.newLine();
            bw.write("em andamento : " + emAndamento); bw.newLine();
            bw.write(String.format("receita total: r$ %.2f", totalRec)); bw.newLine();
            bw.write("========================================"); bw.newLine();

            sucesso = true;
        } catch (IOException e) {
            System.err.println("[arq] erro ao exportar txt: " + e.getMessage());
        } finally {
            // s05: limpa o buffer e fecha o arquivo de texto com seguranca
            fechar(bw);
            System.out.println("[arq] exportar txt -> " + (sucesso ? "ok: " + path : "FALHOU"));
        }
    }

    //funções auxiliares
    // s05: rotina automatica para fechar qualquer arquivo que implemente closeable sem quebrar o codigo
    // polimorfismo puro: serve tanto para object stream quanto para buffered writer. o catch ignora se der erro ao fechar
    private static void fechar(Closeable c) {
        if (c != null) {
            try { c.close(); } catch (IOException ignorada) { /* erro ignorado de proposito no encerramento */ }
        }
    }

    // s03: devolve um container dadosparksys zerado para nao dar nullpointer na primeira inicializacao do software
    private static DadosParkSys dadosVazios() {
        return new DadosParkSys(new HashMap<>(), new ArrayList<>(), new ArrayList<>());
    }

}
