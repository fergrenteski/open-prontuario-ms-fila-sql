package br.com.openprontuario.fila.infrastructure.persistence;

import br.com.openprontuario.fila.application.unidade.ports.UnidadeSaudeRepository;
import br.com.openprontuario.fila.domain.exception.UnidadeEmUsoException;
import br.com.openprontuario.fila.domain.model.TipoUnidade;
import br.com.openprontuario.fila.domain.model.UnidadeSaude;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class UnidadeSaudeRepositoryAdapter implements UnidadeSaudeRepository {

    private final UnidadeJpaRepository unidades;
    private final LotacaoJpaRepository lotacoes;
    private final RegistroFilaJpaRepository registros;

    public UnidadeSaudeRepositoryAdapter(UnidadeJpaRepository unidades, LotacaoJpaRepository lotacoes,
                                         RegistroFilaJpaRepository registros) {
        this.unidades = unidades;
        this.lotacoes = lotacoes;
        this.registros = registros;
    }

    @Override
    public UnidadeSaude salvar(UnidadeSaude unidade) {
        return unidades.save(UnidadeEntity.de(unidade)).paraDominio();
    }

    @Override
    public Optional<UnidadeSaude> buscarPorId(String id) {
        return unidades.findById(id).map(UnidadeEntity::paraDominio);
    }

    @Override
    public List<UnidadeSaude> listar(TipoUnidade tipo) {
        List<UnidadeEntity> lista = tipo == null ? unidades.findAllByOrderByNome() : unidades.findByTipoOrderByNome(tipo);
        return lista.stream().map(UnidadeEntity::paraDominio).toList();
    }

    @Override
    public List<UnidadeSaude> listarPorUsuarioLotado(String usuarioId) {
        return unidades.findLotadasDoUsuario(usuarioId).stream().map(UnidadeEntity::paraDominio).toList();
    }

    @Override
    public boolean emUso(String id) {
        return lotacoes.existsByUnidadeId(id) || registros.existsByUnidadeId(id);
    }

    @Override
    public void remover(String id) {
        try {
            unidades.deleteById(id);
        } catch (DataIntegrityViolationException e) {
            // corrida com uma lotação/registro criado entre a verificação e o DELETE (FK no banco)
            throw new UnidadeEmUsoException("Unidade " + id + " possui lotações ou registros de fila");
        }
    }
}
