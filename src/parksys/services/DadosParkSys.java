package parksys.services;

import parksys.entities.Mensalista;
import parksys.entities.Registro;
import parksys.entities.Vaga;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

// s03: value object p/ agrupar as 3 structs em um unico obj serializavel
// ObjectOutputStream.writeObject() aceita 1 obj por chamada
// sem esta classe, precisariamos de 3 chamadas separadas c/ risco de inconsistencia
// implementa Serializable pq vai ser gravado/lido via ObjectOutputStream/ObjectInputStream
public class DadosParkSys implements Serializable {

    // serialVersionUID = identificador de versao p/ controle de compatib. na deser.
    // se a classe mudar e o uid nao, a deser. de arq. antigo lanca InvalidClassException
    private static final long serialVersionUID = 1L;

    private final Map<String, Vaga> vagas;
    private final List<Registro>    registros;
    private final List<Mensalista>  mensalistas;

    public DadosParkSys(Map<String, Vaga> vagas,
                        List<Registro>    registros,
                        List<Mensalista>  mensalistas) {
        this.vagas       = vagas;
        this.registros   = registros;
        this.mensalistas = mensalistas;
    }

    public Map<String, Vaga> getVagas()       { return vagas;       }
    public List<Registro>    getRegistros()   { return registros;   }
    public List<Mensalista>  getMensalistas() { return mensalistas; }
}