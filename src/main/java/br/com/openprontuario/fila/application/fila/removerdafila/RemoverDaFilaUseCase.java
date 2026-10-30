package br.com.openprontuario.fila.application.fila.removerdafila;

import br.com.openprontuario.fila.application.fila.TransicaoRegistroFila;
import br.com.openprontuario.fila.application.fila.ports.EventPublisherPort;
import br.com.openprontuario.fila.application.fila.ports.PacienteRepository;
import br.com.openprontuario.fila.application.fila.ports.RegistroFilaRepository;
import br.com.openprontuario.fila.application.lotacao.ports.LotacaoRepository;
import br.com.openprontuario.fila.domain.event.EventoFila;
import br.com.openprontuario.fila.domain.model.MotivoSaida;

import java.time.Clock;

/** RF14 — encerra o registro (→ FINALIZADO, sem apagar); publica PACIENTE_REMOVIDO. */
public class RemoverDaFilaUseCase {

    private final TransicaoRegistroFila transicao;

    public RemoverDaFilaUseCase(RegistroFilaRepository registros, LotacaoRepository lotacoes,
                                PacienteRepository pacientes, EventPublisherPort eventos, Clock relogio) {
        this.transicao = new TransicaoRegistroFila(registros, lotacoes, pacientes, eventos, relogio);
    }

    public void executar(String registroId, MotivoSaida motivo, String usuarioId) {
        transicao.aplicar(registroId, usuarioId, EventoFila.PACIENTE_REMOVIDO,
                (registro, agora) -> registro.encerrar(motivo, agora));
    }
}
