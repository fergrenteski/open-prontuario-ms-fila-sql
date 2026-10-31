package br.com.openprontuario.fila.infrastructure.messaging;

import br.com.openprontuario.fila.application.fila.ports.EventPublisherPort;
import br.com.openprontuario.fila.domain.event.EventoDominioFila;
import com.azure.messaging.servicebus.ServiceBusMessage;
import com.azure.messaging.servicebus.ServiceBusSenderClient;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.util.UUID;

/**
 * Publica no tópico {@code eventos-atendimento}: messageId = eventoId (deduplicação do
 * Service Bus), subject = tipo, correlationId = X-Correlation-Id da requisição (MDC) ou, se
 * ausente, um UUID novo — a mensagem nunca sai sem correlationId.
 */
public class ServiceBusEventPublisherAdapter implements EventPublisherPort, AutoCloseable {

    private static final Logger LOG = LoggerFactory.getLogger(ServiceBusEventPublisherAdapter.class);
    private static final ObjectMapper JSON = new ObjectMapper();

    private final ServiceBusSenderClient sender;

    public ServiceBusEventPublisherAdapter(ServiceBusSenderClient sender) {
        this.sender = sender;
    }

    @Override
    public void publicar(EventoDominioFila evento) {
        ServiceBusMessage msg = new ServiceBusMessage(serializar(evento));
        msg.setMessageId(evento.eventoId().toString());
        msg.setSubject(evento.tipo().name());
        msg.setContentType("application/json");
        msg.setCorrelationId(correlationId());
        sender.sendMessage(msg);
        LOG.info("Evento {} {} publicado (registro {}, unidade {})", evento.tipo(), evento.eventoId(),
                evento.registroId(), evento.unidadeId());
    }

    /** A Function rejeita mensagem sem correlationId: fora de uma requisição HTTP (MDC vazio), gera um. */
    private static String correlationId() {
        String doMdc = MDC.get("correlationId");
        return doMdc == null || doMdc.isBlank() ? UUID.randomUUID().toString() : doMdc;
    }

    static byte[] serializar(EventoDominioFila evento) {
        try {
            return JSON.writeValueAsBytes(EventoFilaMessage.de(evento));
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Falha ao serializar evento " + evento.eventoId(), e);
        }
    }

    @Override
    public void close() {
        sender.close();
    }
}
