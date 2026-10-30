package br.com.openprontuario.fila.domain.exception;

/** Unidade possui lotações ou registros de fila e não pode ser removida (409). */
public class UnidadeEmUsoException extends DomainException {
    public UnidadeEmUsoException(String mensagem) {
        super(mensagem);
    }
}
