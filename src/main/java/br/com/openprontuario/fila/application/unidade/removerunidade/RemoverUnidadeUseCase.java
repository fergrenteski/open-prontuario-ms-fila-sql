package br.com.openprontuario.fila.application.unidade.removerunidade;

import br.com.openprontuario.fila.application.unidade.ports.UnidadeSaudeRepository;
import br.com.openprontuario.fila.domain.exception.RecursoNaoEncontradoException;
import br.com.openprontuario.fila.domain.exception.UnidadeEmUsoException;

/** Remove uma unidade sem lotações nem registros de fila (o histórico da fila nunca é apagado). */
public class RemoverUnidadeUseCase {

    private final UnidadeSaudeRepository unidades;

    public RemoverUnidadeUseCase(UnidadeSaudeRepository unidades) {
        this.unidades = unidades;
    }

    public void executar(String unidadeId) {
        if (unidades.buscarPorId(unidadeId).isEmpty()) {
            throw new RecursoNaoEncontradoException("Unidade", unidadeId);
        }
        if (unidades.emUso(unidadeId)) {
            throw new UnidadeEmUsoException("Unidade " + unidadeId + " possui lotações ou registros de fila");
        }
        unidades.remover(unidadeId);
    }
}
