package br.com.openprontuario.fila.application.unidade;

import br.com.openprontuario.fila.application.unidade.atualizarunidade.AtualizarUnidadeUseCase;
import br.com.openprontuario.fila.application.unidade.buscarunidade.BuscarUnidadeUseCase;
import br.com.openprontuario.fila.application.unidade.criarunidade.CriarUnidadeUseCase;
import br.com.openprontuario.fila.application.unidade.listarminhas.ListarMinhasUnidadesUseCase;
import br.com.openprontuario.fila.application.unidade.listarproximas.ListarUnidadesProximasUseCase;
import br.com.openprontuario.fila.application.unidade.listarproximas.UnidadeProximaResult;
import br.com.openprontuario.fila.application.unidade.listarunidades.ListarUnidadesUseCase;
import br.com.openprontuario.fila.application.unidade.ports.UnidadeSaudeRepository;
import br.com.openprontuario.fila.application.unidade.removerunidade.RemoverUnidadeUseCase;
import br.com.openprontuario.fila.domain.exception.RecursoNaoEncontradoException;
import br.com.openprontuario.fila.domain.exception.UnidadeEmUsoException;
import br.com.openprontuario.fila.domain.model.TipoUnidade;
import br.com.openprontuario.fila.domain.model.UnidadeSaude;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UnidadeUseCasesTest {

    static final UnidadeSaude UPA_01 = new UnidadeSaude("upa-01", "UPA Boa Vista", TipoUnidade.UPA, -25.3866, -49.2346);
    static final UnidadeSaude UPA_02 = new UnidadeSaude("upa-02", "UPA Cajuru", TipoUnidade.UPA, -25.4486, -49.2183);
    static final UnidadeSaude UBS_03 = new UnidadeSaude("ubs-03", "UBS Mãe Curitibana", TipoUnidade.UBS, -25.4372, -49.2700);

    private final UnidadeSaudeRepository unidades = mock(UnidadeSaudeRepository.class);

    @Test
    void listarDelegaOFiltroDeTipo() {
        when(unidades.listar(TipoUnidade.UBS)).thenReturn(List.of(UBS_03));
        assertThat(new ListarUnidadesUseCase(unidades).executar(TipoUnidade.UBS)).containsExactly(UBS_03);
    }

    @Test
    void proximasFiltraPeloRaioEOrdenaPorDistancia() {
        when(unidades.listar(null)).thenReturn(List.of(UBS_03, UPA_02, UPA_01));

        // ponto ao lado da UPA Cajuru: UBS_03 (~5,3 km) entra, UPA_01 (~7,1 km) fica fora com raio 6
        List<UnidadeProximaResult> r = new ListarUnidadesProximasUseCase(unidades).executar(-25.4486, -49.2183, 6);

        assertThat(r).extracting(x -> x.unidade().id()).containsExactly("upa-02", "ubs-03");
        assertThat(r.get(0).distanciaKm()).isZero();
        assertThat(r.get(1).distanciaKm()).isBetween(5.0, 6.0);
    }

    @Test
    void minhasUnidadesVemDaLotacao() {
        when(unidades.listarPorUsuarioLotado("usr-prof-03")).thenReturn(List.of(UPA_01));
        assertThat(new ListarMinhasUnidadesUseCase(unidades).executar("usr-prof-03")).containsExactly(UPA_01);
    }

    @Test
    void buscarDevolveOu404() {
        when(unidades.buscarPorId("upa-01")).thenReturn(Optional.of(UPA_01));
        BuscarUnidadeUseCase uc = new BuscarUnidadeUseCase(unidades);

        assertThat(uc.executar("upa-01")).isEqualTo(UPA_01);
        assertThatThrownBy(() -> uc.executar("x")).isInstanceOf(RecursoNaoEncontradoException.class);
    }

    @Test
    void criarGeraIdESalva() {
        when(unidades.salvar(any())).thenAnswer(i -> i.getArgument(0));

        UnidadeSaude criada = new CriarUnidadeUseCase(unidades)
                .executar(new DadosUnidadeCommand("UBS Nova", TipoUnidade.UBS, -25.4, -49.3));

        assertThat(criada.id()).isNotBlank();
        assertThat(criada.nome()).isEqualTo("UBS Nova");
        verify(unidades).salvar(criada);
    }

    @Test
    void atualizarMantemIdOu404() {
        when(unidades.buscarPorId("upa-01")).thenReturn(Optional.of(UPA_01));
        when(unidades.salvar(any())).thenAnswer(i -> i.getArgument(0));
        AtualizarUnidadeUseCase uc = new AtualizarUnidadeUseCase(unidades);
        DadosUnidadeCommand dados = new DadosUnidadeCommand("UPA Boa Vista 24h", TipoUnidade.UPA, -25.39, -49.23);

        UnidadeSaude atualizada = uc.executar("upa-01", dados);

        assertThat(atualizada.id()).isEqualTo("upa-01");
        assertThat(atualizada.nome()).isEqualTo("UPA Boa Vista 24h");
        assertThatThrownBy(() -> uc.executar("x", dados)).isInstanceOf(RecursoNaoEncontradoException.class);
    }

    @Test
    void removerSoQuandoExisteENaoEstaEmUso() {
        RemoverUnidadeUseCase uc = new RemoverUnidadeUseCase(unidades);
        when(unidades.buscarPorId("upa-01")).thenReturn(Optional.of(UPA_01));
        when(unidades.buscarPorId("ubs-03")).thenReturn(Optional.of(UBS_03));
        when(unidades.emUso("upa-01")).thenReturn(true);

        assertThatThrownBy(() -> uc.executar("x")).isInstanceOf(RecursoNaoEncontradoException.class);
        assertThatThrownBy(() -> uc.executar("upa-01")).isInstanceOf(UnidadeEmUsoException.class);
        verify(unidades, never()).remover("upa-01");

        uc.executar("ubs-03");
        verify(unidades).remover("ubs-03");
    }
}
