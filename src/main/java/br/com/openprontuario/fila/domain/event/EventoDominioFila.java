package br.com.openprontuario.fila.domain.event;

import br.com.openprontuario.fila.domain.model.Prioridade;
import br.com.openprontuario.fila.domain.model.RegistroFila;
import br.com.openprontuario.fila.domain.model.StatusFila;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Evento de domínio de uma transição da fila. Sem dados pessoais (LGPD): não carrega
 * pacienteId, nome nem sintomas.
 */
public record EventoDominioFila(UUID eventoId, EventoFila tipo, String registroId, String unidadeId,
                                StatusFila statusAnterior, StatusFila statusAtual, Prioridade prioridade,
                                OffsetDateTime ocorridoEm) {

    public static EventoDominioFila de(EventoFila tipo, RegistroFila registro, StatusFila anterior,
                                       OffsetDateTime agora) {
        return new EventoDominioFila(UUID.randomUUID(), tipo, registro.getId(), registro.getUnidadeId(), anterior,
                registro.getStatus(), registro.getPrioridade(), agora);
    }
}
