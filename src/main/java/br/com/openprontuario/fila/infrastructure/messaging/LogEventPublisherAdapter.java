package br.com.openprontuario.fila.infrastructure.messaging;

import br.com.openprontuario.fila.application.fila.ports.EventPublisherPort;
import br.com.openprontuario.fila.domain.event.EventoDominioFila;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;

/**
 * Modo de desenvolvimento explícito, usado só quando SERVICEBUS_CONNECTION_STRING está vazia:
 * escreve no log a mensagem que seria publicada. A inicialização emite um WARN avisando.
 */
public class LogEventPublisherAdapter implements EventPublisherPort {

    private static final Logger LOG = LoggerFactory.getLogger(LogEventPublisherAdapter.class);

    @Override
    public void publicar(EventoDominioFila evento) {
        LOG.info("[mensageria desabilitada] evento não enviado ao Service Bus: {}",
                new String(ServiceBusEventPublisherAdapter.serializar(evento), StandardCharsets.UTF_8));
    }
}
