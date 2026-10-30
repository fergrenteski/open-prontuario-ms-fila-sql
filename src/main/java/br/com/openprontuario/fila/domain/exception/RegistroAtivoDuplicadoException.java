package br.com.openprontuario.fila.domain.exception;

/** Paciente já possui registro ativo na unidade (409). */
public class RegistroAtivoDuplicadoException extends DomainException {
    public RegistroAtivoDuplicadoException(String mensagem) {
        super(mensagem);
    }
}
