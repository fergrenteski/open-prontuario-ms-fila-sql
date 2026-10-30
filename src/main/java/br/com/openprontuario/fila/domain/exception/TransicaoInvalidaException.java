package br.com.openprontuario.fila.domain.exception;

/** Transição de estado não permitida pela máquina de estados do RegistroFila (409). */
public class TransicaoInvalidaException extends DomainException {
    public TransicaoInvalidaException(String mensagem) {
        super(mensagem);
    }
}
