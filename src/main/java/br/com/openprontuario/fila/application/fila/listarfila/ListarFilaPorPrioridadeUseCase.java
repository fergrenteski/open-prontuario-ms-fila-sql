package br.com.openprontuario.fila.application.fila.listarfila;

import br.com.openprontuario.fila.application.fila.RegistroFilaResult;
import br.com.openprontuario.fila.application.fila.ports.PacienteRepository;
import br.com.openprontuario.fila.application.fila.ports.RegistroFilaRepository;
import br.com.openprontuario.fila.application.lotacao.ports.LotacaoRepository;
import br.com.openprontuario.fila.application.unidade.ports.UnidadeSaudeRepository;
import br.com.openprontuario.fila.domain.exception.AcessoNegadoException;
import br.com.openprontuario.fila.domain.exception.RecursoNaoEncontradoException;
import br.com.openprontuario.fila.domain.model.RegistroFila;
import br.com.openprontuario.fila.domain.model.StatusFila;
import br.com.openprontuario.fila.domain.policy.OrdenacaoFilaPolicy;

import java.util.List;
import java.util.Map;

/** RF08/RF09 — fila ativa da unidade, ordenada por prioridade; exige lotação na unidade. */
public class ListarFilaPorPrioridadeUseCase {

    private final UnidadeSaudeRepository unidades;
    private final LotacaoRepository lotacoes;
    private final RegistroFilaRepository registros;
    private final PacienteRepository pacientes;

    public ListarFilaPorPrioridadeUseCase(UnidadeSaudeRepository unidades, LotacaoRepository lotacoes,
                                          RegistroFilaRepository registros, PacienteRepository pacientes) {
        this.unidades = unidades;
        this.lotacoes = lotacoes;
        this.registros = registros;
        this.pacientes = pacientes;
    }

    /** @param status filtro opcional ({@code null} = todos os ativos) */
    public List<RegistroFilaResult> executar(String unidadeId, String usuarioId, StatusFila status) {
        if (unidades.buscarPorId(unidadeId).isEmpty()) {
            throw new RecursoNaoEncontradoException("Unidade", unidadeId);
        }
        if (!lotacoes.estaLotado(usuarioId, unidadeId)) {
            throw new AcessoNegadoException("Usuário não está lotado na unidade " + unidadeId);
        }
        List<RegistroFila> fila = OrdenacaoFilaPolicy.ordenar(registros.listarAtivosPorUnidade(unidadeId)).stream()
                .filter(r -> status == null || r.getStatus() == status)
                .toList();
        Map<String, String> nomes = pacientes.buscarNomes(fila.stream().map(RegistroFila::getPacienteId).toList());
        return fila.stream().map(r -> new RegistroFilaResult(r, nomes.get(r.getPacienteId()))).toList();
    }
}
