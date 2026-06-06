package parksys.enums;

// enum que representa o estado atual de uma vaga do estacionamento
public enum StatusVaga {
    // cada status tem: descrição em texto e se está disponível para uso
    LIVRE    ("Livre",     true),  // vaga pode receber um veículo
    OCUPADA  ("Ocupada",   false), // vaga já tem um veículo
    RESERVADA("Reservada", false); // vaga reservada para mensalista

    // atributos de cada status
    private String  descricao;  // texto descritivo do status
    private boolean disponivel; // true = pode ser usada, false = não pode

    // construtor do enum
    StatusVaga(String descricao, boolean disponivel) {
        this.descricao  = descricao;
        this.disponivel = disponivel;
    }

    // retorna a descrição textual (ex: "Livre")
    public String getDescricao() {
        return descricao;
    }

    // retorna true se a vaga pode receber um veículo
    public boolean isDisponivel() {
        return disponivel;
    }

    // usado quando o enum é convertido para String
    @Override
    public String toString() {
        return descricao;
    }
}