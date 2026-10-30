package br.com.openprontuario.fila.application.unidade.listarunidades;

import br.com.openprontuario.fila.application.unidade.ports.UnidadeSaudeRepository;
import br.com.openprontuario.fila.domain.model.TipoUnidade;
import br.com.openprontuario.fila.domain.model.UnidadeSaude;

import java.util.List;

/** Lista as unidades, opcionalmente filtrando por tipo. */
public class ListarUnidadesUseCase {

    private final UnidadeSaudeRepository unidades;

    public ListarUnidadesUseCase(UnidadeSaudeRepository unidades) {
        this.unidades = unidades;
    }

    public List<UnidadeSaude> executar(TipoUnidade tipo) {
        return unidades.listar(tipo);
    }
}
