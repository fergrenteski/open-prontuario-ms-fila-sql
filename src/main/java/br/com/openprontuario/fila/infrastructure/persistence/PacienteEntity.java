package br.com.openprontuario.fila.infrastructure.persistence;

import br.com.openprontuario.fila.domain.model.Paciente;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "pacientes")
class PacienteEntity {

    @Id
    private String id;
    private String nome;
    private String cpf;
    @Column(name = "cartao_sus")
    private String cartaoSus;

    protected PacienteEntity() {
    }

    static PacienteEntity de(Paciente p) {
        PacienteEntity e = new PacienteEntity();
        e.id = p.id();
        e.nome = p.nome();
        e.cpf = p.cpf();
        e.cartaoSus = p.cartaoSus();
        return e;
    }

    Paciente paraDominio() {
        return new Paciente(id, nome, cpf, cartaoSus);
    }

    String getId() {
        return id;
    }

    String getNome() {
        return nome;
    }
}
