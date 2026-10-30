package br.com.openprontuario.fila.domain.model;

/** Paciente identificado pelo oid do provedor de identidade. CPF e cartão SUS são opcionais. */
public record Paciente(String id, String nome, String cpf, String cartaoSus) {

    public static Paciente novo(String id, String nome) {
        return new Paciente(id, nome, null, null);
    }

    /** Atualiza o nome de exibição; nome vazio mantém o atual. */
    public Paciente comNome(String novoNome) {
        return novoNome == null || novoNome.isBlank() ? this : new Paciente(id, novoNome, cpf, cartaoSus);
    }
}
