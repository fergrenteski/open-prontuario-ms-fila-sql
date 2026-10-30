package br.com.openprontuario.fila.infrastructure.persistence;

import br.com.openprontuario.fila.application.lotacao.ports.LotacaoRepository;
import br.com.openprontuario.fila.domain.exception.LotacaoDuplicadaException;
import br.com.openprontuario.fila.domain.model.Lotacao;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class LotacaoRepositoryAdapter implements LotacaoRepository {

    private final LotacaoJpaRepository lotacoes;

    public LotacaoRepositoryAdapter(LotacaoJpaRepository lotacoes) {
        this.lotacoes = lotacoes;
    }

    @Override
    public Lotacao salvar(Lotacao lotacao) {
        try {
            return lotacoes.saveAndFlush(LotacaoEntity.de(lotacao)).paraDominio();
        } catch (DataIntegrityViolationException e) {
            // índice único (usuario_id, unidade_id)
            throw new LotacaoDuplicadaException("Usuário " + lotacao.usuarioId() + " já está lotado em "
                    + lotacao.unidadeId());
        }
    }

    @Override
    public Optional<Lotacao> buscarPorId(String id) {
        return lotacoes.findById(id).map(LotacaoEntity::paraDominio);
    }

    @Override
    public List<Lotacao> listarPorUnidade(String unidadeId) {
        return lotacoes.findByUnidadeIdOrderByUsuarioId(unidadeId).stream().map(LotacaoEntity::paraDominio).toList();
    }

    @Override
    public List<Lotacao> listarPorUsuario(String usuarioId) {
        return lotacoes.findByUsuarioIdOrderByUnidadeId(usuarioId).stream().map(LotacaoEntity::paraDominio).toList();
    }

    @Override
    public boolean estaLotado(String usuarioId, String unidadeId) {
        return lotacoes.existsByUsuarioIdAndUnidadeId(usuarioId, unidadeId);
    }

    @Override
    public void remover(String id) {
        lotacoes.deleteById(id);
    }
}
