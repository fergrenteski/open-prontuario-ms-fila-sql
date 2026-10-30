package br.com.openprontuario.fila.application.unidade.listarminhas;

import br.com.openprontuario.fila.application.unidade.ports.UnidadeSaudeRepository;
import br.com.openprontuario.fila.domain.model.UnidadeSaude;

import java.util.List;

/** RF07 — unidades em que o usuário autenticado está lotado. */
public class ListarMinhasUnidadesUseCase {

    private final UnidadeSaudeRepository unidades;

    public ListarMinhasUnidadesUseCase(UnidadeSaudeRepository unidades) {
        this.unidades = unidades;
    }

    public List<UnidadeSaude> executar(String usuarioId) {
        return unidades.listarPorUsuarioLotado(usuarioId);
    }
}
