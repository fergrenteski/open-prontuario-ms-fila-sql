package br.com.openprontuario.fila.domain.exception;

/** Usuário sem permissão para a operação, ex.: não lotado na unidade (403). */
public class AcessoNegadoException extends DomainException {
    public AcessoNegadoException(String mensagem) {
        super(mensagem);
    }
}
