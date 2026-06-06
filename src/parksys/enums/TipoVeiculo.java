package parksys.enums;

// enum que representa os tipos de veículo que o estacionamento aceita
public enum TipoVeiculo {

    // cada tipo tem: nome para exibir, preço por hora, e quantas vagas ocupa
    MOTO ("Motocicleta", 5.00,  1),
    CARRO ("Automóvel", 10.00,  1),
    SUV ("Caminhonete/SUV",18.00,  2),
    CAMINHAO("Caminhão", 30.00,  3);

    // atributos de cada tipo de veículo
    private String nomeExibicao; // nome legível para mostrar na tela
    private double tarifaHora; // quanto custa por hora
    private int vagasOcupadas;// quantas vagas físicas esse veículo precisa

    // construtor do enum — chamado automaticamente para cada valor acima
    TipoVeiculo(String nomeExibicao, double tarifaHora, int vagasOcupadas) {
        this.nomeExibicao = nomeExibicao;
        this.tarifaHora = tarifaHora;
        this.vagasOcupadas = vagasOcupadas;
    }

    // retorna o preço por hora desse tipo de veículo
    public double getTarifaHora() {
        return tarifaHora;
    }

    // retorna quantas vagas esse veículo ocupa
    public int getVagasOcupadas() {
        return vagasOcupadas;
    }

    // retorna o nome legível (ex: "Automóvel")
    public String getNomeExibicao() {
        return nomeExibicao;
    }

    // usado automaticamente quando o enum é convertido para String (ex: no ComboBox)
    @Override
    public String toString() {
        return nomeExibicao;
    }
}