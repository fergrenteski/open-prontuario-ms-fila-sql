package br.com.openprontuario.fila.application.fila.ports;

import br.com.openprontuario.fila.domain.model.Paciente;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

/** Porta de saída para persistência de pacientes. */
public interface PacienteRepository {

    Paciente salvar(Paciente paciente);

    Optional<Paciente> buscarPorId(String id);

    /** Nome de exibição por id (ids sem nome ficam fora do mapa). */
    Map<String, String> buscarNomes(Collection<String> ids);
}
