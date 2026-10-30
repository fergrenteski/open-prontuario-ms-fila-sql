package br.com.openprontuario.fila.bootstrap;

import br.com.openprontuario.fila.application.fila.ports.EventPublisherPort;
import br.com.openprontuario.fila.infrastructure.messaging.LogEventPublisherAdapter;
import br.com.openprontuario.fila.infrastructure.messaging.ServiceBusEventPublisherAdapter;
import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Escolhe o publicador de eventos conforme SERVICEBUS_CONNECTION_STRING. */
@Configuration
public class MensageriaConfig {

    private static final Logger LOG = LoggerFactory.getLogger(MensageriaConfig.class);

    @Bean
    EventPublisherPort eventPublisher(@Value("${openprontuario.servicebus.connection-string:}") String connectionString,
                                      @Value("${openprontuario.servicebus.topic}") String topico) {
        if (connectionString.isBlank()) {
            LOG.warn("SERVICEBUS_CONNECTION_STRING vazia: mensageria desabilitada. Os eventos da fila serão apenas "
                    + "logados e NÃO chegarão à Function (modo de desenvolvimento).");
            return new LogEventPublisherAdapter();
        }
        LOG.info("Publicando eventos da fila no tópico '{}' do Service Bus", topico);
        return new ServiceBusEventPublisherAdapter(new ServiceBusClientBuilder()
                .connectionString(connectionString)
                .sender()
                .topicName(topico)
                .buildClient());
    }
}
