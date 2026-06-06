package parksys.entities;

import parksys.enums.TipoVeiculo;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

// representa o registro de entrada/saída de um veículo
// o serializable permite salvar/carregar este objeto em arquivo .ser
// Comparable<Registro> permite ordenar registros por data
public class Registro implements Serializable, Comparable<Registro>{
    
    // número de versão para controle de compatibilidade na serialização
    private static final long serialVersionUID = 1L;

    private String        placa;       // placa do veículo
    private TipoVeiculo   tipoVeiculo; // tipo do veículo
    private List<String>  idsVagas;    // vagas ocupadas (1, 2 ou 3 dependendo do tipo)
    private LocalDateTime dataEntrada; // momento de entrada
    private LocalDateTime dataSaida;   // momento de saída (null enquanto ainda está aqui)
    private double        valorPago;   // valor cobrado ao sair
    private boolean       mensalista;  // true se o dono é mensalista

    // após carregar do arquivo, threadOrigem virá como null
    // isso acontece porque campos transient são ignorados pelo ObjectOutputStream
    private transient String threadOrigem;

    public Registro(String placa, TipoVeiculo tipoVeiculo,
                    List<String> idsVagas, LocalDateTime dataEntrada) {
        this.placa       = placa;
        this.tipoVeiculo = tipoVeiculo;
        this.idsVagas    = idsVagas;
        this.dataEntrada = dataEntrada;
    }
    
    // do registro mais antigo para o mais recente
    // o TreeSet usa este método para manter os registros em ordem cronológica
    @Override
    public int compareTo(Registro outro) {
        int resultado = this.dataEntrada.compareTo(outro.dataEntrada);
        if (resultado != 0) {
            return resultado;
        }
        // desempate pela placa, para evitar duplicatas no TreeSet
        return this.placa.compareTo(outro.placa);
    }

    public String        getPlaca()        { return placa;        }
    public TipoVeiculo   getTipoVeiculo()  { return tipoVeiculo;  }
    public List<String>  getIdsVagas()     { return idsVagas;     }
    public LocalDateTime getDataEntrada()  { return dataEntrada;  }
    public LocalDateTime getDataSaida()    { return dataSaida;    }
    public double        getValorPago()    { return valorPago;    }
    public boolean       isMensalista()    { return mensalista;   }

    // retorna null se o objeto foi carregado de arquivo (campo transient)
    public String        getThreadOrigem() { return threadOrigem; }

    public void setDataSaida(LocalDateTime dataSaida)   { this.dataSaida    = dataSaida;    }
    public void setValorPago(double valorPago)           { this.valorPago    = valorPago;    }
    public void setMensalista(boolean mensalista)        { this.mensalista   = mensalista;   }
    public void setThreadOrigem(String threadOrigem)     { this.threadOrigem = threadOrigem; }

    @Override
    public String toString() {
        return placa + " | " + tipoVeiculo.getNomeExibicao()
                + " | Vagas: " + idsVagas
                + " | Entrada: " + dataEntrada;
    }
}
