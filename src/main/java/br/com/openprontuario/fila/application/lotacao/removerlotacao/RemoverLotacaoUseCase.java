package br.com.openprontuario.fila.application.lotacao.removerlotacao;

import br.com.openprontuario.fila.application.lotacao.ports.LotacaoRepository;
import br.com.openprontuario.fila.domain.exception.RecursoNaoEncontradoException;

public class RemoverLotacaoUseCase {

    private final LotacaoRepository lotacoes;

    public RemoverLotacaoUseCase(LotacaoRepository lotacoes) {
        this.lotacoes = lotacoes;
    }

    public void executar(String lotacaoId) {
        if (lotacoes.buscarPorId(lotacaoId).isEmpty()) {
            throw new RecursoNaoEncontradoException("Lotação", lotacaoId);
        }
        lotacoes.remover(lotacaoId);
    }
}
