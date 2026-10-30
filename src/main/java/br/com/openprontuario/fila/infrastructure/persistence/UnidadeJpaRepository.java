package br.com.openprontuario.fila.infrastructure.persistence;

import br.com.openprontuario.fila.domain.model.TipoUnidade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

interface UnidadeJpaRepository extends JpaRepository<UnidadeEntity, String> {

    List<UnidadeEntity> findAllByOrderByNome();

    List<UnidadeEntity> findByTipoOrderByNome(TipoUnidade tipo);

    @Query("select u from UnidadeEntity u where u.id in "
            + "(select l.unidadeId from LotacaoEntity l where l.usuarioId = :usuarioId) order by u.nome")
    List<UnidadeEntity> findLotadasDoUsuario(String usuarioId);
}
