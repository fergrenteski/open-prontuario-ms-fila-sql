package br.com.openprontuario.fila.application.unidade.atualizarunidade;

import br.com.openprontuario.fila.application.unidade.DadosUnidadeCommand;
import br.com.openprontuario.fila.application.unidade.ports.UnidadeSaudeRepository;
import br.com.openprontuario.fila.domain.exception.RecursoNaoEncontradoException;
import br.com.openprontuario.fila.domain.model.UnidadeSaude;

public class AtualizarUnidadeUseCase {

    private final UnidadeSaudeRepository unidades;

    public AtualizarUnidadeUseCase(UnidadeSaudeRepository unidades) {
        this.unidades = unidades;
    }

    public UnidadeSaude executar(String unidadeId, DadosUnidadeCommand dados) {
        UnidadeSaude atual = unidades.buscarPorId(unidadeId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Unidade", unidadeId));
        return unidades.salvar(atual.atualizada(dados.nome(), dados.tipo(), dados.latitude(), dados.longitude()));
    }
}
