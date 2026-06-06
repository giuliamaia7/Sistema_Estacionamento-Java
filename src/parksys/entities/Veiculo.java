package parksys.entities;

import parksys.enums.TipoVeiculo;
import java.io.Serializable;

// representa um veículo que entra no estacionamento
// Serializable permite salvar/carregar este objeto em arquivo .ser
public class Veiculo implements Serializable {

    // número de versão para controle de compatibilidade na serialização
    private static final long serialVersionUID = 1L;

    private String      placa; // placa do veículo, ex: "ABC1234"
    private TipoVeiculo tipo;  // tipo: CARRO, MOTO, SUV ou CAMINHAO

    // cria um veículo com placa e tipo definidos
    public Veiculo(String placa, TipoVeiculo tipo) {
        this.placa = placa;
        this.tipo  = tipo;
    }

    public String getPlaca() {
        return placa;
    }

    public TipoVeiculo getTipo() {
        return tipo;
    }

    @Override
    public String toString() {
        return placa + " (" + tipo.getNomeExibicao() + ")";
    }
}
