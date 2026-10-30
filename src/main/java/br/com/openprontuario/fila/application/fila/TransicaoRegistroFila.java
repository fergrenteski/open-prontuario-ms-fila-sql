package br.com.openprontuario.fila.application.fila;

import br.com.openprontuario.fila.application.fila.ports.EventPublisherPort;
import br.com.openprontuario.fila.application.fila.ports.PacienteRepository;
import br.com.openprontuario.fila.application.fila.ports.RegistroFilaRepository;
import br.com.openprontuario.fila.application.lotacao.ports.LotacaoRepository;
import br.com.openprontuario.fila.domain.event.EventoDominioFila;
import br.com.openprontuario.fila.domain.event.EventoFila;
import br.com.openprontuario.fila.domain.exception.AcessoNegadoException;
import br.com.openprontuario.fila.domain.exception.RecursoNaoEncontradoException;
import br.com.openprontuario.fila.domain.model.Paciente;
import br.com.openprontuario.fila.domain.model.RegistroFila;
import br.com.openprontuario.fila.domain.model.StatusFila;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.function.BiConsumer;

/**
 * Fluxo comum às ações do atendente sobre um registro existente: busca (404), verifica a
 * lotação na unidade do registro (403), aplica a transição do domínio (409 se inválida),
 * salva e só então publica o evento.
 */
public class TransicaoRegistroFila {

    private final RegistroFilaRepository registros;
    private final LotacaoRepository lotacoes;
    private final PacienteRepository pacientes;
    private final EventPublisherPort eventos;
    private final Clock relogio;

    public TransicaoRegistroFila(RegistroFilaRepository registros, LotacaoRepository lotacoes,
                                 PacienteRepository pacientes, EventPublisherPort eventos, Clock relogio) {
        this.registros = registros;
        this.lotacoes = lotacoes;
        this.pacientes = pacientes;
        this.eventos = eventos;
        this.relogio = relogio;
    }

    public RegistroFilaResult aplicar(String registroId, String usuarioId, EventoFila tipo,
                                      BiConsumer<RegistroFila, OffsetDateTime> transicao) {
        RegistroFila registro = registros.buscarPorId(registroId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Registro de fila", registroId));
        if (!lotacoes.estaLotado(usuarioId, registro.getUnidadeId())) {
            throw new AcessoNegadoException("Usuário não está lotado na unidade do registro");
        }
        StatusFila anterior = registro.getStatus();
        OffsetDateTime agora = OffsetDateTime.now(relogio);
        transicao.accept(registro, agora);
        RegistroFila salvo = registros.salvar(registro);
        PublicacaoDeEventos.publicar(eventos, EventoDominioFila.de(tipo, salvo, anterior, agora));
        String nome = pacientes.buscarPorId(salvo.getPacienteId()).map(Paciente::nome).orElse(null);
        return new RegistroFilaResult(salvo, nome);
    }
}
