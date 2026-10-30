package br.com.openprontuario.fila.application.fila.avisarchegada;

import br.com.openprontuario.fila.application.fila.PublicacaoDeEventos;
import br.com.openprontuario.fila.application.fila.RegistroFilaResult;
import br.com.openprontuario.fila.application.fila.ports.EventPublisherPort;
import br.com.openprontuario.fila.application.fila.ports.PacienteRepository;
import br.com.openprontuario.fila.application.fila.ports.RegistroFilaRepository;
import br.com.openprontuario.fila.application.unidade.ports.UnidadeSaudeRepository;
import br.com.openprontuario.fila.domain.event.EventoDominioFila;
import br.com.openprontuario.fila.domain.event.EventoFila;
import br.com.openprontuario.fila.domain.exception.RecursoNaoEncontradoException;
import br.com.openprontuario.fila.domain.exception.RegistroAtivoDuplicadoException;
import br.com.openprontuario.fila.domain.model.Paciente;
import br.com.openprontuario.fila.domain.model.RegistroFila;
import br.com.openprontuario.fila.domain.policy.ClassificacaoPreliminarPolicy;

import java.time.Clock;
import java.time.OffsetDateTime;

/**
 * RF03/RF04 — paciente avisa que está a caminho: upsert do paciente, classificação
 * preliminar pelos sintomas, registro A_CAMINHO e evento PACIENTE_A_CAMINHO.
 */
public class AvisarChegadaUseCase {

    private final UnidadeSaudeRepository unidades;
    private final PacienteRepository pacientes;
    private final RegistroFilaRepository registros;
    private final EventPublisherPort eventos;
    private final Clock relogio;

    public AvisarChegadaUseCase(UnidadeSaudeRepository unidades, PacienteRepository pacientes,
                                RegistroFilaRepository registros, EventPublisherPort eventos, Clock relogio) {
        this.unidades = unidades;
        this.pacientes = pacientes;
        this.registros = registros;
        this.eventos = eventos;
        this.relogio = relogio;
    }

    public RegistroFilaResult executar(AvisarChegadaCommand cmd) {
        if (unidades.buscarPorId(cmd.unidadeId()).isEmpty()) {
            throw new RecursoNaoEncontradoException("Unidade", cmd.unidadeId());
        }
        if (registros.existeAtivo(cmd.pacienteId(), cmd.unidadeId())) {
            throw new RegistroAtivoDuplicadoException("Paciente já possui registro ativo na unidade " + cmd.unidadeId());
        }
        Paciente paciente = pacientes.salvar(pacientes.buscarPorId(cmd.pacienteId())
                .map(p -> p.comNome(cmd.pacienteNome()))
                .orElseGet(() -> Paciente.novo(cmd.pacienteId(), cmd.pacienteNome())));

        OffsetDateTime agora = OffsetDateTime.now(relogio);
        RegistroFila registro = RegistroFila.avisar(cmd.pacienteId(), cmd.unidadeId(), cmd.sintomas(),
                cmd.tempoEstimadoChegadaMin(), ClassificacaoPreliminarPolicy.classificar(cmd.sintomas()), agora);
        RegistroFila salvo = registros.salvar(registro);
        PublicacaoDeEventos.publicar(eventos, EventoDominioFila.de(EventoFila.PACIENTE_A_CAMINHO, salvo, null, agora));
        return new RegistroFilaResult(salvo, paciente.nome());
    }
}
