package br.com.openprontuario.fila.application.lotacao.ports;

import br.com.openprontuario.fila.domain.model.Lotacao;

import java.util.List;
import java.util.Optional;

/** Porta de saída para persistência de lotações. */
public interface LotacaoRepository {

    Lotacao salvar(Lotacao lotacao);

    Optional<Lotacao> buscarPorId(String id);

    List<Lotacao> listarPorUnidade(String unidadeId);

    List<Lotacao> listarPorUsuario(String usuarioId);

    boolean estaLotado(String usuarioId, String unidadeId);

    void remover(String id);
}
