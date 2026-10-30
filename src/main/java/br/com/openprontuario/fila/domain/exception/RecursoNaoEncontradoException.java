package br.com.openprontuario.fila.domain.exception;

/** Recurso inexistente (404). */
public class RecursoNaoEncontradoException extends DomainException {
    public RecursoNaoEncontradoException(String recurso, String id) {
        super(recurso + " não encontrado(a): " + id);
    }
}
