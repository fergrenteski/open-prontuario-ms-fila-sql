package br.com.openprontuario.fila.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

interface LotacaoJpaRepository extends JpaRepository<LotacaoEntity, String> {

    List<LotacaoEntity> findByUnidadeIdOrderByUsuarioId(String unidadeId);

    List<LotacaoEntity> findByUsuarioIdOrderByUnidadeId(String usuarioId);

    boolean existsByUsuarioIdAndUnidadeId(String usuarioId, String unidadeId);

    boolean existsByUnidadeId(String unidadeId);
}
