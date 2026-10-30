package br.com.openprontuario.fila.application.unidade.buscarunidade;

import br.com.openprontuario.fila.application.unidade.ports.UnidadeSaudeRepository;
import br.com.openprontuario.fila.domain.exception.RecursoNaoEncontradoException;
import br.com.openprontuario.fila.domain.model.UnidadeSaude;

public class BuscarUnidadeUseCase {

    private final UnidadeSaudeRepository unidades;

    public BuscarUnidadeUseCase(UnidadeSaudeRepository unidades) {
        this.unidades = unidades;
    }

    public UnidadeSaude executar(String unidadeId) {
        return unidades.buscarPorId(unidadeId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Unidade", unidadeId));
    }
}
