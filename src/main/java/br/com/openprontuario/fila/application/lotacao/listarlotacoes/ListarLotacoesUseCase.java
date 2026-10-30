package br.com.openprontuario.fila.application.lotacao.listarlotacoes;

import br.com.openprontuario.fila.application.lotacao.ports.LotacaoRepository;
import br.com.openprontuario.fila.domain.model.Lotacao;

import java.util.List;

/** Lotações de uma unidade. */
public class ListarLotacoesUseCase {

    private final LotacaoRepository lotacoes;

    public ListarLotacoesUseCase(LotacaoRepository lotacoes) {
        this.lotacoes = lotacoes;
    }

    public List<Lotacao> executar(String unidadeId) {
        return lotacoes.listarPorUnidade(unidadeId);
    }
}
