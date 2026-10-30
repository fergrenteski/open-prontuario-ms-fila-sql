package br.com.openprontuario.fila.infrastructure.messaging;

import br.com.openprontuario.fila.domain.event.EventoDominioFila;

import java.time.format.DateTimeFormatter;

/** Corpo JSON da mensagem, exatamente como o schema {@code EventoFilaMessage} do asyncapi.yaml. */
record EventoFilaMessage(String eventoId, String tipo, String registroId, String unidadeId, String statusAnterior,
                         String statusAtual, String prioridade, String ocorridoEm, int versao) {

    static final int VERSAO = 1;

    static EventoFilaMessage de(EventoDominioFila e) {
        return new EventoFilaMessage(e.eventoId().toString(), e.tipo().name(), e.registroId(), e.unidadeId(),
                e.statusAnterior() == null ? null : e.statusAnterior().name(), e.statusAtual().name(),
                e.prioridade().name(), DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(e.ocorridoEm()), VERSAO);
    }
}
