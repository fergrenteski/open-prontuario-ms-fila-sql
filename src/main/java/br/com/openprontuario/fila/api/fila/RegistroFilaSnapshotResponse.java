package br.com.openprontuario.fila.api.fila;

import br.com.openprontuario.fila.domain.model.Prioridade;
import br.com.openprontuario.fila.domain.model.RegistroFila;
import br.com.openprontuario.fila.domain.model.StatusFila;

import java.time.OffsetDateTime;

/** Sem dados pessoais: só o necessário para a Function recalcular a visão agregada. */
public record RegistroFilaSnapshotResponse(String id, String unidadeId, StatusFila status, Prioridade prioridade,
                                           OffsetDateTime horarioAviso, OffsetDateTime horarioChegada,
                                           OffsetDateTime horarioInicioAtendimento) {

    static RegistroFilaSnapshotResponse de(RegistroFila r) {
        return new RegistroFilaSnapshotResponse(r.getId(), r.getUnidadeId(), r.getStatus(), r.getPrioridade(),
                r.getHorarioAviso(), r.getHorarioChegada(), r.getHorarioInicioAtendimento());
    }
}
