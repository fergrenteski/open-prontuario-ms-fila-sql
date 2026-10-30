package br.com.openprontuario.fila.infrastructure.messaging;

import br.com.openprontuario.fila.domain.event.EventoDominioFila;
import br.com.openprontuario.fila.domain.event.EventoFila;
import br.com.openprontuario.fila.domain.model.Prioridade;
import br.com.openprontuario.fila.domain.model.StatusFila;
import com.azure.messaging.servicebus.ServiceBusMessage;
import com.azure.messaging.servicebus.ServiceBusSenderClient;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.slf4j.MDC;

import java.time.OffsetDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class ServiceBusEventPublisherAdapterTest {

    private final ServiceBusSenderClient sender = mock(ServiceBusSenderClient.class);
    private final ServiceBusEventPublisherAdapter adapter = new ServiceBusEventPublisherAdapter(sender);

    private final EventoDominioFila evento = new EventoDominioFila(
            UUID.fromString("1b0e7c52-1d0a-4c84-9a77-0f0e2b6c9a10"), EventoFila.PACIENTE_A_CAMINHO,
            "3c9a7f20-5b1e-4d8a-9f6c-2e7b1a4d8c55", "upa-01", null, StatusFila.A_CAMINHO,
            Prioridade.MUITO_URGENTE, OffsetDateTime.parse("2026-10-14T08:05:00-03:00"));

    @AfterEach
    void limparMdc() {
        MDC.clear();
    }

    @Test
    void enviaMensagemComPropriedadesAmqpECorpoDoContrato() throws Exception {
        MDC.put("correlationId", "8f2c1e7a-4b6d-4c1f-9a3e-2d5b7c9e1f04");

        adapter.publicar(evento);

        ArgumentCaptor<ServiceBusMessage> captor = ArgumentCaptor.forClass(ServiceBusMessage.class);
        verify(sender).sendMessage(captor.capture());
        ServiceBusMessage msg = captor.getValue();
        assertThat(msg.getMessageId()).isEqualTo("1b0e7c52-1d0a-4c84-9a77-0f0e2b6c9a10");
        assertThat(msg.getSubject()).isEqualTo("PACIENTE_A_CAMINHO");
        assertThat(msg.getCorrelationId()).isEqualTo("8f2c1e7a-4b6d-4c1f-9a3e-2d5b7c9e1f04");
        assertThat(msg.getContentType()).isEqualTo("application/json");

        JsonNode corpo = new ObjectMapper().readTree(msg.getBody().toBytes());
        assertThat(corpo).isEqualTo(new ObjectMapper().readTree("""
                {"eventoId":"1b0e7c52-1d0a-4c84-9a77-0f0e2b6c9a10","tipo":"PACIENTE_A_CAMINHO",
                 "registroId":"3c9a7f20-5b1e-4d8a-9f6c-2e7b1a4d8c55","unidadeId":"upa-01",
                 "statusAnterior":null,"statusAtual":"A_CAMINHO","prioridade":"MUITO_URGENTE",
                 "ocorridoEm":"2026-10-14T08:05:00-03:00","versao":1}
                """));
    }

    @Test
    void statusAnteriorPreenchidoQuandoHouver() throws Exception {
        adapter.publicar(new EventoDominioFila(UUID.randomUUID(), EventoFila.PACIENTE_CHEGOU, "r", "upa-01",
                StatusFila.A_CAMINHO, StatusFila.AGUARDANDO, Prioridade.URGENTE, OffsetDateTime.now()));

        ArgumentCaptor<ServiceBusMessage> captor = ArgumentCaptor.forClass(ServiceBusMessage.class);
        verify(sender).sendMessage(captor.capture());
        JsonNode corpo = new ObjectMapper().readTree(captor.getValue().getBody().toBytes());
        assertThat(corpo.get("statusAnterior").asText()).isEqualTo("A_CAMINHO");
        assertThat(captor.getValue().getCorrelationId()).isNull();
    }

    @Test
    void propagaFalhaDoSdkParaOCasoDeUsoDecidir() {
        doThrow(new IllegalStateException("conexão recusada")).when(sender).sendMessage(any());
        assertThatThrownBy(() -> adapter.publicar(evento)).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void fechaOSenderNoShutdown() {
        adapter.close();
        verify(sender).close();
    }

    @Test
    void adapterDeLogNaoFalha() {
        new LogEventPublisherAdapter().publicar(evento);
    }
}
