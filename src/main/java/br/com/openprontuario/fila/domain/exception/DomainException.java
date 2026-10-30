package br.com.openprontuario.fila.domain.exception;

/** Raiz das exceções de negócio. A API converte cada subclasse em Problem Details. */
public abstract class DomainException extends RuntimeException {
    protected DomainException(String mensagem) {
        super(mensagem);
    }
}
