package br.com.openprontuario.fila.application.lotacao.criarlotacao;

import br.com.openprontuario.fila.application.lotacao.ports.LotacaoRepository;
import br.com.openprontuario.fila.application.unidade.ports.UnidadeSaudeRepository;
import br.com.openprontuario.fila.domain.exception.LotacaoDuplicadaException;
import br.com.openprontuario.fila.domain.exception.RecursoNaoEncontradoException;
import br.com.openprontuario.fila.domain.model.Lotacao;

/** Vincula um profissional a uma unidade existente; um vínculo por (usuário, unidade). */
public class CriarLotacaoUseCase {

    private final LotacaoRepository lotacoes;
    private final UnidadeSaudeRepository unidades;

    public CriarLotacaoUseCase(LotacaoRepository lotacoes, UnidadeSaudeRepository unidades) {
        this.lotacoes = lotacoes;
        this.unidades = unidades;
    }

    public Lotacao executar(CriarLotacaoCommand cmd) {
        if (unidades.buscarPorId(cmd.unidadeId()).isEmpty()) {
            throw new RecursoNaoEncontradoException("Unidade", cmd.unidadeId());
        }
        if (lotacoes.estaLotado(cmd.usuarioId(), cmd.unidadeId())) {
            throw new LotacaoDuplicadaException("Usuário " + cmd.usuarioId() + " já está lotado em " + cmd.unidadeId());
        }
        return lotacoes.salvar(Lotacao.nova(cmd.usuarioId(), cmd.unidadeId(), cmd.perfil()));
    }
}
