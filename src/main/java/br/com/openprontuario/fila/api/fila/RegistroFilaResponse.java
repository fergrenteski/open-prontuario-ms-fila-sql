package br.com.openprontuario.fila.api.fila;

import br.com.openprontuario.fila.application.fila.RegistroFilaResult;
import br.com.openprontuario.fila.domain.model.MotivoSaida;
import br.com.openprontuario.fila.domain.model.Prioridade;
import br.com.openprontuario.fila.domain.model.StatusFila;

import java.time.OffsetDateTime;
import java.util.List;

public record RegistroFilaResponse(String id, String pacienteId, String pacienteNome, String unidadeId,
                                   Prioridade prioridade, StatusFila status, List<String> sintomas,
                                   Integer tempoEstimadoChegadaMin, OffsetDateTime horarioAviso,
                                   OffsetDateTime horarioChegada, OffsetDateTime horarioInicioAtendimento,
                                   MotivoSaida motivoSaida) {

    static RegistroFilaResponse de(RegistroFilaResult resultado) {
        var r = resultado.registro();
        return new RegistroFilaResponse(r.getId(), r.getPacienteId(), resultado.pacienteNome(), r.getUnidadeId(),
                r.getPrioridade(), r.getStatus(), r.getSintomas(), r.getTempoEstimadoChegadaMin(), r.getHorarioAviso(),
                r.getHorarioChegada(), r.getHorarioInicioAtendimento(), r.getMotivoSaida());
    }
}
