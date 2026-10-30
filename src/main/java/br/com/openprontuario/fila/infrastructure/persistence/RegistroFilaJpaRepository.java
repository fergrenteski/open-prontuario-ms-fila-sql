package br.com.openprontuario.fila.infrastructure.persistence;

import br.com.openprontuario.fila.domain.model.StatusFila;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.OffsetDateTime;
import java.util.List;

interface RegistroFilaJpaRepository extends JpaRepository<RegistroFilaEntity, String> {

    List<RegistroFilaEntity> findByUnidadeIdAndStatusNot(String unidadeId, StatusFila status);

    boolean existsByPacienteIdAndUnidadeIdAndStatusNot(String pacienteId, String unidadeId, StatusFila status);

    boolean existsByUnidadeId(String unidadeId);

    @Query("select r from RegistroFilaEntity r where r.unidadeId = :unidadeId and "
            + "(r.status <> br.com.openprontuario.fila.domain.model.StatusFila.FINALIZADO "
            + "or r.horarioInicioAtendimento >= :desde)")
    List<RegistroFilaEntity> findSnapshot(String unidadeId, OffsetDateTime desde);
}
