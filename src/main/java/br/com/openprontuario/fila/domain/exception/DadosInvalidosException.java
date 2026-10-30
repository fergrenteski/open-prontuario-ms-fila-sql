package br.com.openprontuario.fila.domain.exception;

/** Dados que violam uma invariante do domínio (400). */
public class DadosInvalidosException extends DomainException {
    public DadosInvalidosException(String mensagem) {
        super(mensagem);
    }
}
