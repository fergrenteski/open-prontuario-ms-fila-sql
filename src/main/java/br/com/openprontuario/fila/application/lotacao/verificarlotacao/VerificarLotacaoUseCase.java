package br.com.openprontuario.fila.application.lotacao.verificarlotacao;

import br.com.openprontuario.fila.application.lotacao.ports.LotacaoRepository;

/** Responde se o usuário está lotado na unidade (rota interna usada pelo MS Prontuário). */
public class VerificarLotacaoUseCase {

    private final LotacaoRepository lotacoes;

    public VerificarLotacaoUseCase(LotacaoRepository lotacoes) {
        this.lotacoes = lotacoes;
    }

    public boolean executar(String usuarioId, String unidadeId) {
        return lotacoes.estaLotado(usuarioId, unidadeId);
    }
}
