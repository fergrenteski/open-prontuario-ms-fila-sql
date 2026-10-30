package br.com.openprontuario.fila.application.lotacao;

import br.com.openprontuario.fila.application.lotacao.criarlotacao.CriarLotacaoCommand;
import br.com.openprontuario.fila.application.lotacao.criarlotacao.CriarLotacaoUseCase;
import br.com.openprontuario.fila.application.lotacao.listarlotacoes.ListarLotacoesUseCase;
import br.com.openprontuario.fila.application.lotacao.listarlotacoesdousuario.ListarLotacoesDoUsuarioUseCase;
import br.com.openprontuario.fila.application.lotacao.ports.LotacaoRepository;
import br.com.openprontuario.fila.application.lotacao.removerlotacao.RemoverLotacaoUseCase;
import br.com.openprontuario.fila.application.lotacao.verificarlotacao.VerificarLotacaoUseCase;
import br.com.openprontuario.fila.application.unidade.ports.UnidadeSaudeRepository;
import br.com.openprontuario.fila.domain.exception.LotacaoDuplicadaException;
import br.com.openprontuario.fila.domain.exception.RecursoNaoEncontradoException;
import br.com.openprontuario.fila.domain.model.Lotacao;
import br.com.openprontuario.fila.domain.model.PerfilUsuario;
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

class LotacaoUseCasesTest {

    private final LotacaoRepository lotacoes = mock(LotacaoRepository.class);
    private final UnidadeSaudeRepository unidades = mock(UnidadeSaudeRepository.class);
    private final Lotacao lot1 = new Lotacao("lot-1", "usr-atendente-07", "upa-01", PerfilUsuario.ATENDENTE);

    @Test
    void listagens() {
        when(lotacoes.listarPorUnidade("upa-01")).thenReturn(List.of(lot1));
        when(lotacoes.listarPorUsuario("usr-atendente-07")).thenReturn(List.of(lot1));

        assertThat(new ListarLotacoesUseCase(lotacoes).executar("upa-01")).containsExactly(lot1);
        assertThat(new ListarLotacoesDoUsuarioUseCase(lotacoes).executar("usr-atendente-07")).containsExactly(lot1);
    }

    @Test
    void verificar() {
        when(lotacoes.estaLotado("usr-atendente-07", "upa-01")).thenReturn(true);
        VerificarLotacaoUseCase uc = new VerificarLotacaoUseCase(lotacoes);

        assertThat(uc.executar("usr-atendente-07", "upa-01")).isTrue();
        assertThat(uc.executar("usr-atendente-07", "ubs-03")).isFalse();
    }

    @Test
    void criarExigeUnidadeExistenteENaoDuplicada() {
        CriarLotacaoUseCase uc = new CriarLotacaoUseCase(lotacoes, unidades);
        when(unidades.buscarPorId("upa-01"))
                .thenReturn(Optional.of(new UnidadeSaude("upa-01", "UPA", TipoUnidade.UPA, 0, 0)));
        when(lotacoes.estaLotado("usr-atendente-07", "upa-01")).thenReturn(true);
        when(lotacoes.salvar(any())).thenAnswer(i -> i.getArgument(0));

        assertThatThrownBy(() -> uc.executar(new CriarLotacaoCommand("u", "x", PerfilUsuario.ATENDENTE)))
                .isInstanceOf(RecursoNaoEncontradoException.class);
        assertThatThrownBy(() -> uc.executar(new CriarLotacaoCommand("usr-atendente-07", "upa-01", PerfilUsuario.ATENDENTE)))
                .isInstanceOf(LotacaoDuplicadaException.class);

        Lotacao nova = uc.executar(new CriarLotacaoCommand("usr-novo", "upa-01", PerfilUsuario.PROFISSIONAL));
        assertThat(nova.id()).isNotBlank();
        assertThat(nova.perfil()).isEqualTo(PerfilUsuario.PROFISSIONAL);
        verify(lotacoes).salvar(nova);
    }

    @Test
    void removerExigeExistencia() {
        RemoverLotacaoUseCase uc = new RemoverLotacaoUseCase(lotacoes);
        when(lotacoes.buscarPorId("lot-1")).thenReturn(Optional.of(lot1));

        assertThatThrownBy(() -> uc.executar("x")).isInstanceOf(RecursoNaoEncontradoException.class);
        verify(lotacoes, never()).remover("x");
        uc.executar("lot-1");
        verify(lotacoes).remover("lot-1");
    }
}
