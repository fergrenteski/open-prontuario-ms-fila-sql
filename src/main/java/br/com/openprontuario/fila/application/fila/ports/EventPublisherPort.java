package br.com.openprontuario.fila.application.fila.ports;

import br.com.openprontuario.fila.domain.event.EventoDominioFila;

/** Porta de saída para publicar eventos de mudança de estado da fila. */
public interface EventPublisherPort {

    void publicar(EventoDominioFila evento);
}
