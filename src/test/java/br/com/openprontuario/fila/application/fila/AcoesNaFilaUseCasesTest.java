package br.com.openprontuario.fila.application.fila;

import br.com.openprontuario.fila.application.fila.alterarprioridade.AlterarPrioridadeUseCase;
import br.com.openprontuario.fila.application.fila.confirmarchegada.ConfirmarChegadaUseCase;
import br.com.openprontuario.fila.application.fila.iniciaratendimento.IniciarAtendimentoUseCase;
import br.com.openprontuario.fila.application.fila.ports.EventPublisherPort;
import br.com.openprontuario.fila.application.fila.ports.PacienteRepository;
import br.com.openprontuario.fila.application.fila.ports.RegistroFilaRepository;
import br.com.openprontuario.fila.application.fila.removerdafila.RemoverDaFilaUseCase;
import br.com.openprontuario.fila.application.lotacao.ports.LotacaoRepository;
import br.com.openprontuario.fila.domain.event.EventoDominioFila;
import br.com.openprontuario.fila.domain.event.EventoFila;
import br.com.openprontuario.fila.domain.exception.AcessoNegadoException;
import br.com.openprontuario.fila.domain.exception.RecursoNaoEncontradoException;
import br.com.openprontuario.fila.domain.exception.TransicaoInvalidaException;
import br.com.openprontuario.fila.domain.model.MotivoSaida;
import br.com.openprontuario.fila.domain.model.Paciente;
import br.com.openprontuario.fila.domain.model.Prioridade;
import br.com.openprontuario.fila.domain.model.RegistroFila;
import br.com.openprontuario.fila.domain.model.StatusFila;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static br.com.openprontuario.fila.application.fila.AvisarChegadaUseCaseTest.RELOGIO;
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

class AcoesNaFilaUseCasesTest {

    static final OffsetDateTime AGORA = OffsetDateTime.now(RELOGIO);
    static final String ATENDENTE = "usr-atendente-07";

    private final RegistroFilaRepository registros = mock(RegistroFilaRepository.class);
    private final LotacaoRepository lotacoes = mock(LotacaoRepository.class);
    private final PacienteRepository pacientes = mock(PacienteRepository.class);
    private final EventPublisherPort eventos = mock(EventPublisherPort.class);

    private final ConfirmarChegadaUseCase confirmar =
            new ConfirmarChegadaUseCase(registros, lotacoes, pacientes, eventos, RELOGIO);
    private final AlterarPrioridadeUseCase alterar =
            new AlterarPrioridadeUseCase(registros, lotacoes, pacientes, eventos, RELOGIO);
    private final IniciarAtendimentoUseCase iniciar =
            new IniciarAtendimentoUseCase(registros, lotacoes, pacientes, eventos, RELOGIO);
    private final RemoverDaFilaUseCase remover =
            new RemoverDaFilaUseCase(registros, lotacoes, pacientes, eventos, RELOGIO);

    private RegistroFila registro;

    @BeforeEach
    void setUp() {
        registro = RegistroFila.avisar("pac-101", "upa-01", List.of("febre"), 10, Prioridade.POUCO_URGENTE,
                AGORA.minusMinutes(30));
        when(registros.buscarPorId(registro.getId())).thenReturn(Optional.of(registro));
        when(registros.salvar(any())).thenAnswer(i -> i.getArgument(0));
        when(lotacoes.estaLotado(ATENDENTE, "upa-01")).thenReturn(true);
        when(pacientes.buscarPorId("pac-101")).thenReturn(Optional.of(Paciente.novo("pac-101", "João Pereira")));
    }

    private EventoDominioFila eventoPublicadoAposSalvar() {
        var ordem = inOrder(registros, eventos);
        ordem.verify(registros).salvar(registro);
        ArgumentCaptor<EventoDominioFila> captor = ArgumentCaptor.forClass(EventoDominioFila.class);
        ordem.verify(eventos).publicar(captor.capture());
        return captor.getValue();
    }

    @Test
    void confirmarChegadaPublicaPacienteChegou() {
        RegistroFilaResult r = confirmar.executar(registro.getId(), ATENDENTE);

        assertThat(r.pacienteNome()).isEqualTo("João Pereira");
        assertThat(r.registro().getStatus()).isEqualTo(StatusFila.AGUARDANDO);
        assertThat(r.registro().getHorarioChegada()).isEqualTo(AGORA);
        EventoDominioFila e = eventoPublicadoAposSalvar();
        assertThat(e.tipo()).isEqualTo(EventoFila.PACIENTE_CHEGOU);
        assertThat(e.statusAnterior()).isEqualTo(StatusFila.A_CAMINHO);
        assertThat(e.statusAtual()).isEqualTo(StatusFila.AGUARDANDO);
        assertThat(e.registroId()).isEqualTo(registro.getId());
        assertThat(e.unidadeId()).isEqualTo("upa-01");
        assertThat(e.ocorridoEm()).isEqualTo(AGORA);
    }

    @Test
    void alterarPrioridadePublicaPrioridadeAlterada() {
        RegistroFilaResult r = alterar.executar(registro.getId(), Prioridade.EMERGENCIA, ATENDENTE);

        assertThat(r.registro().getPrioridade()).isEqualTo(Prioridade.EMERGENCIA);
        EventoDominioFila e = eventoPublicadoAposSalvar();
        assertThat(e.tipo()).isEqualTo(EventoFila.PRIORIDADE_ALTERADA);
        assertThat(e.statusAnterior()).isEqualTo(StatusFila.A_CAMINHO);
        assertThat(e.statusAtual()).isEqualTo(StatusFila.A_CAMINHO);
        assertThat(e.prioridade()).isEqualTo(Prioridade.EMERGENCIA);
    }

    @Test
    void iniciarAtendimentoPublicaAtendimentoIniciado() {
        registro.confirmarChegada(AGORA.minusMinutes(10));

        RegistroFilaResult r = iniciar.executar(registro.getId(), ATENDENTE);

        assertThat(r.registro().getStatus()).isEqualTo(StatusFila.EM_ATENDIMENTO);
        assertThat(r.registro().getHorarioInicioAtendimento()).isEqualTo(AGORA);
        EventoDominioFila e = eventoPublicadoAposSalvar();
        assertThat(e.tipo()).isEqualTo(EventoFila.ATENDIMENTO_INICIADO);
        assertThat(e.statusAnterior()).isEqualTo(StatusFila.AGUARDANDO);
    }

    @Test
    void removerDaFilaFinalizaEPublicaPacienteRemovido() {
        remover.executar(registro.getId(), MotivoSaida.DESISTENCIA, ATENDENTE);

        assertThat(registro.getStatus()).isEqualTo(StatusFila.FINALIZADO);
        assertThat(registro.getMotivoSaida()).isEqualTo(MotivoSaida.DESISTENCIA);
        EventoDominioFila e = eventoPublicadoAposSalvar();
        assertThat(e.tipo()).isEqualTo(EventoFila.PACIENTE_REMOVIDO);
        assertThat(e.statusAnterior()).isEqualTo(StatusFila.A_CAMINHO);
        assertThat(e.statusAtual()).isEqualTo(StatusFila.FINALIZADO);
    }

    @Test
    void usuarioNaoLotadoNaUnidadeDoRegistroEh403() {
        assertThatThrownBy(() -> confirmar.executar(registro.getId(), "usr-prof-de-outra-unidade"))
                .isInstanceOf(AcessoNegadoException.class);
        assertThat(registro.getStatus()).isEqualTo(StatusFila.A_CAMINHO);
        verify(registros, never()).salvar(any());
        verifyNoInteractions(eventos);
    }

    @Test
    void registroInexistenteEh404() {
        assertThatThrownBy(() -> iniciar.executar("nao-existe", ATENDENTE))
                .isInstanceOf(RecursoNaoEncontradoException.class);
        verifyNoInteractions(eventos);
    }

    @Test
    void transicaoInvalidaEh409ENaoSalvaNemPublica() {
        assertThatThrownBy(() -> iniciar.executar(registro.getId(), ATENDENTE))
                .isInstanceOf(TransicaoInvalidaException.class);
        assertThatThrownBy(() -> remover.executar(registro.getId(), MotivoSaida.ATENDIDO, ATENDENTE))
                .isInstanceOf(TransicaoInvalidaException.class);
        verify(registros, never()).salvar(any());
        verifyNoInteractions(eventos);
    }

    @Test
    void falhaNaPublicacaoNaoDesfazATransicao() {
        doThrow(new IllegalStateException("Service Bus fora")).when(eventos).publicar(any());

        RegistroFilaResult r = confirmar.executar(registro.getId(), ATENDENTE);

        assertThat(r.registro().getStatus()).isEqualTo(StatusFila.AGUARDANDO);
        verify(registros).salvar(registro);
    }

    @Test
    void nomeDoPacienteAusenteFicaNulo() {
        when(pacientes.buscarPorId("pac-101")).thenReturn(Optional.empty());
        assertThat(confirmar.executar(registro.getId(), ATENDENTE).pacienteNome()).isNull();
    }
}
