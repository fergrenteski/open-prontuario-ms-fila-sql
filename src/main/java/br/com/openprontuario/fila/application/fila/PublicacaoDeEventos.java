package br.com.openprontuario.fila.application.fila;

import br.com.openprontuario.fila.application.fila.ports.EventPublisherPort;
import br.com.openprontuario.fila.domain.event.EventoDominioFila;

import java.lang.System.Logger.Level;

/**
 * Publica o evento depois que o registro foi salvo. Falha na publicação é logada e não
 * desfaz a operação (débito técnico registrado: Transactional Outbox); a Function recalcula
 * a partir do estado, então o próximo evento da unidade corrige a visão agregada.
 */
public final class PublicacaoDeEventos {

    private static final System.Logger LOG = System.getLogger(PublicacaoDeEventos.class.getName());

    private PublicacaoDeEventos() {
    }

    public static void publicar(EventPublisherPort porta, EventoDominioFila evento) {
        try {
            porta.publicar(evento);
        } catch (RuntimeException e) {
            LOG.log(Level.ERROR, "Falha ao publicar evento " + evento.tipo() + " " + evento.eventoId()
                    + " do registro " + evento.registroId() + "; operação mantida", e);
        }
    }
}
