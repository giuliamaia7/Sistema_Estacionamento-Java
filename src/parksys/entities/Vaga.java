package parksys.entities;

import parksys.enums.StatusVaga;
import java.io.Serializable;

// representa uma vaga física do estacionamento, ex: "A01", "B07"
// Serializable permite salvar/carregar este objeto em arquivo .ser
public class Vaga implements Serializable{

    // número de versão para controle de compatibilidade na serialização
    private static final long serialVersionUID = 1L;

    private String    id;     // identificador único, ex: "A01"
    private StatusVaga status; // estado atual: LIVRE, OCUPADA ou RESERVADA

    // toda vaga começa como LIVRE ao ser criada
    public Vaga(String id) {
        this.id     = id;
        this.status = StatusVaga.LIVRE;
    }

    public String getId() {
        return id;
    }

    public StatusVaga getStatus() {
        return status;
    }

    public void setStatus(StatusVaga status) {
        this.status = status;
    }

    // verifica se a vaga está disponível consultando o próprio enum
    public boolean isDisponivel() {
        return status.isDisponivel();
    }

    @Override
    public String toString() {
        return id + " [" + status.getDescricao() + "]";
    }

}
