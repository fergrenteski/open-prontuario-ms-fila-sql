package br.com.openprontuario.fila.application.fila.alterarprioridade;

import br.com.openprontuario.fila.application.fila.RegistroFilaResult;
import br.com.openprontuario.fila.application.fila.TransicaoRegistroFila;
import br.com.openprontuario.fila.application.fila.ports.EventPublisherPort;
import br.com.openprontuario.fila.application.fila.ports.PacienteRepository;
import br.com.openprontuario.fila.application.fila.ports.RegistroFilaRepository;
import br.com.openprontuario.fila.application.lotacao.ports.LotacaoRepository;
import br.com.openprontuario.fila.domain.event.EventoFila;
import br.com.openprontuario.fila.domain.model.Prioridade;

import java.time.Clock;

/** RF11 — altera a prioridade (A_CAMINHO/AGUARDANDO); publica PRIORIDADE_ALTERADA. */
public class AlterarPrioridadeUseCase {

    private final TransicaoRegistroFila transicao;

    public AlterarPrioridadeUseCase(RegistroFilaRepository registros, LotacaoRepository lotacoes,
                                    PacienteRepository pacientes, EventPublisherPort eventos, Clock relogio) {
        this.transicao = new TransicaoRegistroFila(registros, lotacoes, pacientes, eventos, relogio);
    }

    public RegistroFilaResult executar(String registroId, Prioridade prioridade, String usuarioId) {
        return transicao.aplicar(registroId, usuarioId, EventoFila.PRIORIDADE_ALTERADA,
                (registro, agora) -> registro.alterarPrioridade(prioridade));
    }
}
