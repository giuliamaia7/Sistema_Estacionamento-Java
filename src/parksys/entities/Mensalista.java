package parksys.entities;

import java.io.Serializable;

// mostra cliente mensalista com vaga reservada
// o serializable permite salvar/carregar este objeto em arquivo .ser
public class Mensalista implements Serializable {

    // número de versão para controle
    private static final long serialVersionUID = 1L;

    private String nome;            // nome do cliente
    private String placa;           // placa do veículo dele
    private String idVagaReservada; // vaga exclusiva (fica com StatusVaga.RESERVADA)
    private double mensalidade;     // valor fixo pago por mês

    public Mensalista(String nome, String placa,
                      String idVagaReservada, double mensalidade) {
        this.nome            = nome;
        this.placa           = placa;
        this.idVagaReservada = idVagaReservada;
        this.mensalidade     = mensalidade;
    }

    public String getNome()            { return nome;            }
    public String getPlaca()           { return placa;           }
    public String getIdVagaReservada() { return idVagaReservada; }
    public double getMensalidade()     { return mensalidade;     }

    @Override
    public String toString() {
        return nome + " | " + placa + " | Vaga: " + idVagaReservada
                + String.format(" | R$ %.2f/mes", mensalidade);
    }
}
