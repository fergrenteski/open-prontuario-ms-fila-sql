package br.com.openprontuario.fila.application.lotacao.listarlotacoesdousuario;

import br.com.openprontuario.fila.application.lotacao.ports.LotacaoRepository;
import br.com.openprontuario.fila.domain.model.Lotacao;

import java.util.List;

/** Lotações de um usuário (rota interna usada pelo MS Prontuário). */
public class ListarLotacoesDoUsuarioUseCase {

    private final LotacaoRepository lotacoes;

    public ListarLotacoesDoUsuarioUseCase(LotacaoRepository lotacoes) {
        this.lotacoes = lotacoes;
    }

    public List<Lotacao> executar(String usuarioId) {
        return lotacoes.listarPorUsuario(usuarioId);
    }
}
