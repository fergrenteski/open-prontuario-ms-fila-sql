package br.com.openprontuario.fila.application.fila.confirmarchegada;

import br.com.openprontuario.fila.application.fila.RegistroFilaResult;
import br.com.openprontuario.fila.application.fila.TransicaoRegistroFila;
import br.com.openprontuario.fila.application.fila.ports.EventPublisherPort;
import br.com.openprontuario.fila.application.fila.ports.PacienteRepository;
import br.com.openprontuario.fila.application.fila.ports.RegistroFilaRepository;
import br.com.openprontuario.fila.application.lotacao.ports.LotacaoRepository;
import br.com.openprontuario.fila.domain.event.EventoFila;
import br.com.openprontuario.fila.domain.model.RegistroFila;

import java.time.Clock;

/** RF10 — A_CAMINHO → AGUARDANDO; publica PACIENTE_CHEGOU. */
public class ConfirmarChegadaUseCase {

    private final TransicaoRegistroFila transicao;

    public ConfirmarChegadaUseCase(RegistroFilaRepository registros, LotacaoRepository lotacoes,
            PacienteRepository pacientes, EventPublisherPort eventos, Clock relogio) {
        this.transicao = new TransicaoRegistroFila(registros, lotacoes, pacientes, eventos, relogio);
    }

    public RegistroFilaResult executar(String registroId, String usuarioId) {
        return transicao.aplicar(registroId, usuarioId, EventoFila.PACIENTE_CHEGOU, RegistroFila::confirmarChegada);
    }
}
