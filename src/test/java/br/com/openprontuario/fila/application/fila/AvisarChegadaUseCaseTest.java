package br.com.openprontuario.fila.application.fila;

import br.com.openprontuario.fila.application.fila.avisarchegada.AvisarChegadaCommand;
import br.com.openprontuario.fila.application.fila.avisarchegada.AvisarChegadaUseCase;
import br.com.openprontuario.fila.application.fila.ports.EventPublisherPort;
import br.com.openprontuario.fila.application.fila.ports.PacienteRepository;
import br.com.openprontuario.fila.application.fila.ports.RegistroFilaRepository;
import br.com.openprontuario.fila.application.unidade.ports.UnidadeSaudeRepository;
import br.com.openprontuario.fila.domain.event.EventoDominioFila;
import br.com.openprontuario.fila.domain.event.EventoFila;
import br.com.openprontuario.fila.domain.exception.RecursoNaoEncontradoException;
import br.com.openprontuario.fila.domain.exception.RegistroAtivoDuplicadoException;
import br.com.openprontuario.fila.domain.model.Paciente;
import br.com.openprontuario.fila.domain.model.Prioridade;
import br.com.openprontuario.fila.domain.model.StatusFila;
import br.com.openprontuario.fila.domain.model.TipoUnidade;
import br.com.openprontuario.fila.domain.model.UnidadeSaude;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class AvisarChegadaUseCaseTest {

    static final Clock RELOGIO = Clock.fixed(Instant.parse("2026-10-14T11:05:00Z"), ZoneId.of("America/Sao_Paulo"));

    private final UnidadeSaudeRepository unidades = mock(UnidadeSaudeRepository.class);
    private final PacienteRepository pacientes = mock(PacienteRepository.class);
    private final RegistroFilaRepository registros = mock(RegistroFilaRepository.class);
    private final EventPublisherPort eventos = mock(EventPublisherPort.class);
    private final AvisarChegadaUseCase uc = new AvisarChegadaUseCase(unidades, pacientes, registros, eventos, RELOGIO);

    private final AvisarChegadaCommand cmd = new AvisarChegadaCommand("pac-101", "João Pereira", "upa-01",
            List.of("Dor no peito", "tontura"), 20);

    @BeforeEach
    void setUp() {
        when(unidades.buscarPorId("upa-01"))
                .thenReturn(Optional.of(new UnidadeSaude("upa-01", "UPA Boa Vista", TipoUnidade.UPA, -25.3866, -49.2346)));
        when(pacientes.salvar(any())).thenAnswer(i -> i.getArgument(0));
        when(registros.salvar(any())).thenAnswer(i -> i.getArgument(0));
    }

    @Test
    void criaRegistroACaminhoClassificadoEPublicaEventoDepoisDeSalvar() {
        RegistroFilaResult r = uc.executar(cmd);

        assertThat(r.pacienteNome()).isEqualTo("João Pereira");
        assertThat(r.registro().getStatus()).isEqualTo(StatusFila.A_CAMINHO);
        assertThat(r.registro().getPrioridade()).isEqualTo(Prioridade.MUITO_URGENTE);
        assertThat(r.registro().getPacienteId()).isEqualTo("pac-101");
        assertThat(r.registro().getTempoEstimadoChegadaMin()).isEqualTo(20);
        assertThat(r.registro().getHorarioAviso()).isEqualTo(OffsetDateTime.parse("2026-10-14T08:05:00-03:00"));

        var ordem = inOrder(registros, eventos);
        ordem.verify(registros).salvar(r.registro());
        ArgumentCaptor<EventoDominioFila> evento = ArgumentCaptor.forClass(EventoDominioFila.class);
        ordem.verify(eventos).publicar(evento.capture());

        EventoDominioFila e = evento.getValue();
        assertThat(e.tipo()).isEqualTo(EventoFila.PACIENTE_A_CAMINHO);
        assertThat(e.registroId()).isEqualTo(r.registro().getId());
        assertThat(e.unidadeId()).isEqualTo("upa-01");
        assertThat(e.statusAnterior()).isNull();
        assertThat(e.statusAtual()).isEqualTo(StatusFila.A_CAMINHO);
        assertThat(e.prioridade()).isEqualTo(Prioridade.MUITO_URGENTE);
        assertThat(e.ocorridoEm()).isEqualTo(r.registro().getHorarioAviso());
    }

    @Test
    void criaPacienteNovoComNomeDoCabecalho() {
        uc.executar(cmd);
        verify(pacientes).salvar(Paciente.novo("pac-101", "João Pereira"));
    }

    @Test
    void atualizaNomeDePacienteExistenteMantendoDocumentos() {
        when(pacientes.buscarPorId("pac-101"))
                .thenReturn(Optional.of(new Paciente("pac-101", "João", "11111111111", "700000000000001")));

        uc.executar(cmd);

        verify(pacientes).salvar(new Paciente("pac-101", "João Pereira", "11111111111", "700000000000001"));
    }

    @Test
    void registroAtivoDuplicadoNaUnidadeEh409() {
        when(registros.existeAtivo("pac-101", "upa-01")).thenReturn(true);

        assertThatThrownBy(() -> uc.executar(cmd)).isInstanceOf(RegistroAtivoDuplicadoException.class);
        verify(registros, never()).salvar(any());
        verifyNoInteractions(eventos);
    }

    @Test
    void unidadeInexistenteEh404() {
        assertThatThrownBy(() -> uc.executar(new AvisarChegadaCommand("pac-101", null, "x", List.of("febre"), null)))
                .isInstanceOf(RecursoNaoEncontradoException.class);
        verifyNoInteractions(eventos);
    }

    @Test
    void falhaNaPublicacaoNaoDesfazAOperacao() {
        doThrow(new IllegalStateException("Service Bus fora")).when(eventos).publicar(any());

        RegistroFilaResult r = uc.executar(cmd);

        assertThat(r.registro().getStatus()).isEqualTo(StatusFila.A_CAMINHO);
        verify(registros).salvar(r.registro());
    }
}
