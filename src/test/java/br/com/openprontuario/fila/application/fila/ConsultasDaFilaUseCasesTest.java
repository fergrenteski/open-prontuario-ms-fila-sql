package br.com.openprontuario.fila.application.fila;

import br.com.openprontuario.fila.application.fila.listarfila.ListarFilaPorPrioridadeUseCase;
import br.com.openprontuario.fila.application.fila.ports.PacienteRepository;
import br.com.openprontuario.fila.application.fila.ports.RegistroFilaRepository;
import br.com.openprontuario.fila.application.fila.snapshot.ObterSnapshotFilaUseCase;
import br.com.openprontuario.fila.application.lotacao.ports.LotacaoRepository;
import br.com.openprontuario.fila.application.unidade.ports.UnidadeSaudeRepository;
import br.com.openprontuario.fila.domain.exception.AcessoNegadoException;
import br.com.openprontuario.fila.domain.exception.RecursoNaoEncontradoException;
import br.com.openprontuario.fila.domain.model.Prioridade;
import br.com.openprontuario.fila.domain.model.RegistroFila;
import br.com.openprontuario.fila.domain.model.StatusFila;
import br.com.openprontuario.fila.domain.model.TipoUnidade;
import br.com.openprontuario.fila.domain.model.UnidadeSaude;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static br.com.openprontuario.fila.application.fila.AcoesNaFilaUseCasesTest.AGORA;
import static br.com.openprontuario.fila.application.fila.AvisarChegadaUseCaseTest.RELOGIO;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ConsultasDaFilaUseCasesTest {

    private final UnidadeSaudeRepository unidades = mock(UnidadeSaudeRepository.class);
    private final LotacaoRepository lotacoes = mock(LotacaoRepository.class);
    private final RegistroFilaRepository registros = mock(RegistroFilaRepository.class);
    private final PacienteRepository pacientes = mock(PacienteRepository.class);
    private final ListarFilaPorPrioridadeUseCase listar =
            new ListarFilaPorPrioridadeUseCase(unidades, lotacoes, registros, pacientes);

    private final RegistroFila leve = RegistroFila.avisar("pac-101", "upa-01", List.of("tosse"), null,
            Prioridade.NAO_URGENTE, AGORA.minusMinutes(50));
    private final RegistroFila grave = RegistroFila.avisar("pac-102", "upa-01", List.of("convulsão"), null,
            Prioridade.EMERGENCIA, AGORA.minusMinutes(5));

    @BeforeEach
    void setUp() {
        when(unidades.buscarPorId("upa-01"))
                .thenReturn(Optional.of(new UnidadeSaude("upa-01", "UPA Boa Vista", TipoUnidade.UPA, 0, 0)));
        when(lotacoes.estaLotado("usr-prof-03", "upa-01")).thenReturn(true);
        grave.confirmarChegada(AGORA);
        when(registros.listarAtivosPorUnidade("upa-01")).thenReturn(List.of(leve, grave));
        when(pacientes.buscarNomes(anyCollection())).thenReturn(Map.of("pac-101", "João Pereira"));
    }

    @Test
    void listaOrdenadaPorPrioridadeComNomes() {
        List<RegistroFilaResult> fila = listar.executar("upa-01", "usr-prof-03", null);

        assertThat(fila).extracting(RegistroFilaResult::registro).containsExactly(grave, leve);
        assertThat(fila).extracting(RegistroFilaResult::pacienteNome).containsExactly(null, "João Pereira");
    }

    @Test
    void filtraPorStatus() {
        assertThat(listar.executar("upa-01", "usr-prof-03", StatusFila.A_CAMINHO))
                .extracting(RegistroFilaResult::registro).containsExactly(leve);
        assertThat(listar.executar("upa-01", "usr-prof-03", StatusFila.FINALIZADO)).isEmpty();
    }

    @Test
    void naoLotadoEh403EUnidadeInexistenteEh404() {
        assertThatThrownBy(() -> listar.executar("upa-01", "pac-101", null)).isInstanceOf(AcessoNegadoException.class);
        assertThatThrownBy(() -> listar.executar("x", "usr-prof-03", null))
                .isInstanceOf(RecursoNaoEncontradoException.class);
    }

    @Test
    void snapshotConsultaAtendimentosDasUltimasDuasHoras() {
        when(registros.listarParaSnapshot("upa-01", AGORA.minusHours(2))).thenReturn(List.of(grave));

        assertThat(new ObterSnapshotFilaUseCase(registros, RELOGIO).executar("upa-01")).containsExactly(grave);
    }
}
