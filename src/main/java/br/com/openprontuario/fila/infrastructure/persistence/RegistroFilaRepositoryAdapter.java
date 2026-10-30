package br.com.openprontuario.fila.infrastructure.persistence;

import br.com.openprontuario.fila.application.fila.ports.RegistroFilaRepository;
import br.com.openprontuario.fila.domain.exception.RegistroAtivoDuplicadoException;
import br.com.openprontuario.fila.domain.model.RegistroFila;
import br.com.openprontuario.fila.domain.model.StatusFila;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

@Component
public class RegistroFilaRepositoryAdapter implements RegistroFilaRepository {

    private final RegistroFilaJpaRepository registros;

    public RegistroFilaRepositoryAdapter(RegistroFilaJpaRepository registros) {
        this.registros = registros;
    }

    @Override
    public RegistroFila salvar(RegistroFila registro) {
        try {
            return registros.saveAndFlush(RegistroFilaEntity.de(registro)).paraDominio();
        } catch (DataIntegrityViolationException e) {
            // índice único filtrado: um registro ativo por paciente/unidade (corrida entre dois avisos)
            throw new RegistroAtivoDuplicadoException("Paciente já possui registro ativo na unidade "
                    + registro.getUnidadeId());
        }
    }

    @Override
    public Optional<RegistroFila> buscarPorId(String id) {
        return registros.findById(id).map(RegistroFilaEntity::paraDominio);
    }

    @Override
    public List<RegistroFila> listarAtivosPorUnidade(String unidadeId) {
        return registros.findByUnidadeIdAndStatusNot(unidadeId, StatusFila.FINALIZADO).stream()
                .map(RegistroFilaEntity::paraDominio).toList();
    }

    @Override
    public boolean existeAtivo(String pacienteId, String unidadeId) {
        return registros.existsByPacienteIdAndUnidadeIdAndStatusNot(pacienteId, unidadeId, StatusFila.FINALIZADO);
    }

    @Override
    public List<RegistroFila> listarParaSnapshot(String unidadeId, OffsetDateTime inicioAtendimentoDesde) {
        return registros.findSnapshot(unidadeId, inicioAtendimentoDesde).stream()
                .map(RegistroFilaEntity::paraDominio).toList();
    }
}
