package br.com.openprontuario.fila.application.unidade.criarunidade;

import br.com.openprontuario.fila.application.unidade.DadosUnidadeCommand;
import br.com.openprontuario.fila.application.unidade.ports.UnidadeSaudeRepository;
import br.com.openprontuario.fila.domain.model.UnidadeSaude;

public class CriarUnidadeUseCase {

    private final UnidadeSaudeRepository unidades;

    public CriarUnidadeUseCase(UnidadeSaudeRepository unidades) {
        this.unidades = unidades;
    }

    public UnidadeSaude executar(DadosUnidadeCommand dados) {
        return unidades.salvar(UnidadeSaude.nova(dados.nome(), dados.tipo(), dados.latitude(), dados.longitude()));
    }
}
