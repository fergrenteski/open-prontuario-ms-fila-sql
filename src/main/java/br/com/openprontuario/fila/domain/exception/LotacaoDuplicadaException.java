package br.com.openprontuario.fila.domain.exception;

/** Usuário já está lotado na unidade (409). */
public class LotacaoDuplicadaException extends DomainException {
    public LotacaoDuplicadaException(String mensagem) {
        super(mensagem);
    }
}
