package br.com.openprontuario.fila.infrastructure.persistence;

import br.com.openprontuario.fila.application.fila.ports.PacienteRepository;
import br.com.openprontuario.fila.domain.model.Paciente;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class PacienteRepositoryAdapter implements PacienteRepository {

    private final PacienteJpaRepository pacientes;

    public PacienteRepositoryAdapter(PacienteJpaRepository pacientes) {
        this.pacientes = pacientes;
    }

    @Override
    public Paciente salvar(Paciente paciente) {
        return pacientes.save(PacienteEntity.de(paciente)).paraDominio();
    }

    @Override
    public Optional<Paciente> buscarPorId(String id) {
        return pacientes.findById(id).map(PacienteEntity::paraDominio);
    }

    @Override
    public Map<String, String> buscarNomes(Collection<String> ids) {
        return pacientes.findAllById(ids).stream()
                .filter(p -> p.getNome() != null)
                .collect(Collectors.toMap(PacienteEntity::getId, PacienteEntity::getNome));
    }
}
