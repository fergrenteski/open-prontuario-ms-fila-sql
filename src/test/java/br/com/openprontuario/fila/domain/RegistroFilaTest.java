package br.com.openprontuario.fila.domain;

import br.com.openprontuario.fila.domain.exception.DadosInvalidosException;
import br.com.openprontuario.fila.domain.exception.TransicaoInvalidaException;
import br.com.openprontuario.fila.domain.model.MotivoSaida;
import br.com.openprontuario.fila.domain.model.Prioridade;
import br.com.openprontuario.fila.domain.model.RegistroFila;
import br.com.openprontuario.fila.domain.model.StatusFila;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RegistroFilaTest {

    static final OffsetDateTime T0 = OffsetDateTime.of(2026, 10, 14, 8, 0, 0, 0, ZoneOffset.ofHours(-3));

    /** Cria um registro já no status pedido, percorrendo apenas transições válidas. */
    static RegistroFila emStatus(StatusFila status) {
        RegistroFila r = RegistroFila.avisar("pac-101", "upa-01", List.of("febre"), 15, Prioridade.POUCO_URGENTE, T0);
        if (status == StatusFila.A_CAMINHO) return r;
        if (status == StatusFila.FINALIZADO) {
            r.encerrar(MotivoSaida.DESISTENCIA, T0.plusMinutes(1));
            return r;
        }
        r.confirmarChegada(T0.plusMinutes(10));
        if (status == StatusFila.EM_ATENDIMENTO) r.iniciarAtendimento(T0.plusMinutes(30));
        return r;
    }

    @Test
    void avisarCriaRegistroACaminho() {
        RegistroFila r = RegistroFila.avisar("pac-101", "upa-01", List.of("febre", "tosse"), 15, Prioridade.URGENTE, T0);

        assertThat(r.getId()).isNotBlank();
        assertThat(r.getStatus()).isEqualTo(StatusFila.A_CAMINHO);
        assertThat(r.getPrioridade()).isEqualTo(Prioridade.URGENTE);
        assertThat(r.getSintomas()).containsExactly("febre", "tosse");
        assertThat(r.getTempoEstimadoChegadaMin()).isEqualTo(15);
        assertThat(r.getHorarioAviso()).isEqualTo(T0);
        assertThat(r.getHorarioChegada()).isNull();
        assertThat(r.isAtivo()).isTrue();
    }

    @Test
    void avisarExigeSintomasEPaciente() {
        assertThatThrownBy(() -> RegistroFila.avisar("pac-101", "upa-01", List.of(), null, Prioridade.URGENTE, T0))
                .isInstanceOf(DadosInvalidosException.class);
        assertThatThrownBy(() -> RegistroFila.avisar(" ", "upa-01", List.of("febre"), null, Prioridade.URGENTE, T0))
                .isInstanceOf(DadosInvalidosException.class);
    }

    @Test
    void confirmarChegadaVaiParaAguardando() {
        RegistroFila r = emStatus(StatusFila.A_CAMINHO);
        r.confirmarChegada(T0.plusMinutes(10));
        assertThat(r.getStatus()).isEqualTo(StatusFila.AGUARDANDO);
        assertThat(r.getHorarioChegada()).isEqualTo(T0.plusMinutes(10));
    }

    @Test
    void iniciarAtendimentoVaiParaEmAtendimento() {
        RegistroFila r = emStatus(StatusFila.AGUARDANDO);
        r.iniciarAtendimento(T0.plusMinutes(40));
        assertThat(r.getStatus()).isEqualTo(StatusFila.EM_ATENDIMENTO);
        assertThat(r.getHorarioInicioAtendimento()).isEqualTo(T0.plusMinutes(40));
    }

    @ParameterizedTest
    @EnumSource(value = StatusFila.class, names = {"AGUARDANDO", "EM_ATENDIMENTO", "FINALIZADO"})
    void confirmarChegadaForaDeACaminhoEhInvalido(StatusFila status) {
        RegistroFila r = emStatus(status);
        assertThatThrownBy(() -> r.confirmarChegada(T0)).isInstanceOf(TransicaoInvalidaException.class);
        assertThat(r.getStatus()).isEqualTo(status);
    }

    @ParameterizedTest
    @EnumSource(value = StatusFila.class, names = {"A_CAMINHO", "EM_ATENDIMENTO", "FINALIZADO"})
    void iniciarAtendimentoForaDeAguardandoEhInvalido(StatusFila status) {
        RegistroFila r = emStatus(status);
        assertThatThrownBy(() -> r.iniciarAtendimento(T0)).isInstanceOf(TransicaoInvalidaException.class);
        assertThat(r.getStatus()).isEqualTo(status);
    }

    @ParameterizedTest
    @EnumSource(value = StatusFila.class, names = {"A_CAMINHO", "AGUARDANDO"})
    void alterarPrioridadeAntesDoAtendimento(StatusFila status) {
        RegistroFila r = emStatus(status);
        r.alterarPrioridade(Prioridade.EMERGENCIA);
        assertThat(r.getPrioridade()).isEqualTo(Prioridade.EMERGENCIA);
        assertThat(r.getStatus()).isEqualTo(status);
    }

    @ParameterizedTest
    @EnumSource(value = StatusFila.class, names = {"EM_ATENDIMENTO", "FINALIZADO"})
    void alterarPrioridadeDepoisDoInicioEhInvalido(StatusFila status) {
        RegistroFila r = emStatus(status);
        assertThatThrownBy(() -> r.alterarPrioridade(Prioridade.EMERGENCIA))
                .isInstanceOf(TransicaoInvalidaException.class);
        assertThat(r.getPrioridade()).isEqualTo(Prioridade.POUCO_URGENTE);
    }

    static Stream<Arguments> encerramentos() {
        return Stream.of(
                Arguments.of(StatusFila.A_CAMINHO, MotivoSaida.DESISTENCIA, true),
                Arguments.of(StatusFila.A_CAMINHO, MotivoSaida.ATENDIDO, false),
                Arguments.of(StatusFila.A_CAMINHO, MotivoSaida.ENCAMINHADO, false),
                Arguments.of(StatusFila.AGUARDANDO, MotivoSaida.DESISTENCIA, true),
                Arguments.of(StatusFila.AGUARDANDO, MotivoSaida.ENCAMINHADO, true),
                Arguments.of(StatusFila.AGUARDANDO, MotivoSaida.ATENDIDO, false),
                Arguments.of(StatusFila.EM_ATENDIMENTO, MotivoSaida.ATENDIDO, true),
                Arguments.of(StatusFila.EM_ATENDIMENTO, MotivoSaida.DESISTENCIA, false),
                Arguments.of(StatusFila.EM_ATENDIMENTO, MotivoSaida.ENCAMINHADO, false),
                Arguments.of(StatusFila.FINALIZADO, MotivoSaida.ATENDIDO, false),
                Arguments.of(StatusFila.FINALIZADO, MotivoSaida.DESISTENCIA, false),
                Arguments.of(StatusFila.FINALIZADO, MotivoSaida.ENCAMINHADO, false));
    }

    @ParameterizedTest(name = "{0} encerrado com {1} permitido={2}")
    @MethodSource("encerramentos")
    void encerrarSegueAMaquinaDeEstados(StatusFila status, MotivoSaida motivo, boolean permitido) {
        RegistroFila r = emStatus(status);
        MotivoSaida motivoAnterior = r.getMotivoSaida();
        if (permitido) {
            r.encerrar(motivo, T0.plusHours(1));
            assertThat(r.getStatus()).isEqualTo(StatusFila.FINALIZADO);
            assertThat(r.getMotivoSaida()).isEqualTo(motivo);
            assertThat(r.isAtivo()).isFalse();
        } else {
            assertThatThrownBy(() -> r.encerrar(motivo, T0.plusHours(1)))
                    .isInstanceOf(TransicaoInvalidaException.class);
            assertThat(r.getStatus()).isEqualTo(status);
            assertThat(r.getMotivoSaida()).isEqualTo(motivoAnterior);
        }
    }

    @Test
    void restaurarPreservaTodosOsCampos() {
        RegistroFila r = RegistroFila.restaurar("r1", "pac-102", "upa-02", Prioridade.URGENTE, StatusFila.EM_ATENDIMENTO,
                List.of("vomito"), null, T0, T0.plusMinutes(5), T0.plusMinutes(20), null);

        assertThat(r.getId()).isEqualTo("r1");
        assertThat(r.getPacienteId()).isEqualTo("pac-102");
        assertThat(r.getUnidadeId()).isEqualTo("upa-02");
        assertThat(r.getHorarioChegada()).isEqualTo(T0.plusMinutes(5));
        assertThat(r.getHorarioInicioAtendimento()).isEqualTo(T0.plusMinutes(20));
        assertThat(r.getTempoEstimadoChegadaMin()).isNull();
    }
}
