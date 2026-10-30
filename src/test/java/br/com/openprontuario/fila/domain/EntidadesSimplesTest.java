package br.com.openprontuario.fila.domain;

import br.com.openprontuario.fila.domain.event.EventoDominioFila;
import br.com.openprontuario.fila.domain.event.EventoFila;
import br.com.openprontuario.fila.domain.exception.DadosInvalidosException;
import br.com.openprontuario.fila.domain.exception.RecursoNaoEncontradoException;
import br.com.openprontuario.fila.domain.model.Lotacao;
import br.com.openprontuario.fila.domain.model.Paciente;
import br.com.openprontuario.fila.domain.model.PerfilUsuario;
import br.com.openprontuario.fila.domain.model.Prioridade;
import br.com.openprontuario.fila.domain.model.RegistroFila;
import br.com.openprontuario.fila.domain.model.StatusFila;
import br.com.openprontuario.fila.domain.model.TipoUnidade;
import br.com.openprontuario.fila.domain.model.UnidadeSaude;
import org.junit.jupiter.api.Test;

import java.util.List;

import static br.com.openprontuario.fila.domain.RegistroFilaTest.T0;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;

class EntidadesSimplesTest {

    private final UnidadeSaude upa01 = new UnidadeSaude("upa-01", "UPA Boa Vista", TipoUnidade.UPA, -25.3866, -49.2346);

    @Test
    void haversineDistanciaZeroParaOMesmoPonto() {
        assertThat(upa01.calcularDistancia(-25.3866, -49.2346)).isCloseTo(0.0, within(1e-9));
    }

    @Test
    void haversineUmGrauDeLatitudeValeCercaDe111Km() {
        UnidadeSaude equador = new UnidadeSaude("x", "Equador", TipoUnidade.UBS, 0, 0);
        assertThat(equador.calcularDistancia(1, 0)).isCloseTo(111.19, within(0.01));
    }

    @Test
    void haversineEntreUpaBoaVistaEUpaCajuru() {
        // ~7,1 km em linha reta entre as duas UPAs do seed
        assertThat(upa01.calcularDistancia(-25.4486, -49.2183)).isCloseTo(7.09, within(0.05));
    }

    @Test
    void unidadeValidaInvariantes() {
        assertThatThrownBy(() -> new UnidadeSaude("u", " ", TipoUnidade.UPA, 0, 0)).isInstanceOf(DadosInvalidosException.class);
        assertThatThrownBy(() -> new UnidadeSaude(null, "n", TipoUnidade.UPA, 0, 0)).isInstanceOf(DadosInvalidosException.class);
        assertThatThrownBy(() -> new UnidadeSaude("u", "n", null, 0, 0)).isInstanceOf(DadosInvalidosException.class);
        assertThatThrownBy(() -> new UnidadeSaude("u", "n", TipoUnidade.UPA, 91, 0)).isInstanceOf(DadosInvalidosException.class);
        assertThatThrownBy(() -> new UnidadeSaude("u", "n", TipoUnidade.UPA, -91, 0)).isInstanceOf(DadosInvalidosException.class);
        assertThatThrownBy(() -> new UnidadeSaude("u", "n", TipoUnidade.UPA, 0, 181)).isInstanceOf(DadosInvalidosException.class);
        assertThatThrownBy(() -> new UnidadeSaude("u", "n", TipoUnidade.UPA, 0, -181)).isInstanceOf(DadosInvalidosException.class);
    }

    @Test
    void novaUnidadeGeraIdEAtualizadaMantemId() {
        UnidadeSaude nova = UnidadeSaude.nova("UBS Nova", TipoUnidade.UBS, -25.4, -49.2);
        UnidadeSaude atualizada = nova.atualizada("UBS Renomeada", TipoUnidade.UPA, -25.5, -49.3);

        assertThat(nova.id()).isNotBlank();
        assertThat(atualizada.id()).isEqualTo(nova.id());
        assertThat(atualizada.nome()).isEqualTo("UBS Renomeada");
        assertThat(atualizada.tipo()).isEqualTo(TipoUnidade.UPA);
    }

    @Test
    void lotacaoNovaEValidacoes() {
        Lotacao l = Lotacao.nova("usr-1", "upa-01", PerfilUsuario.ATENDENTE);
        assertThat(l.id()).isNotBlank();
        assertThatThrownBy(() -> Lotacao.nova(" ", "upa-01", PerfilUsuario.ATENDENTE)).isInstanceOf(DadosInvalidosException.class);
        assertThatThrownBy(() -> Lotacao.nova("u", null, PerfilUsuario.ATENDENTE)).isInstanceOf(DadosInvalidosException.class);
        assertThatThrownBy(() -> Lotacao.nova("u", "upa-01", null)).isInstanceOf(DadosInvalidosException.class);
    }

    @Test
    void pacienteComNomeAtualizaSomenteQuandoInformado() {
        Paciente p = new Paciente("pac-101", "João Pereira", "11111111111", "700000000000001");

        assertThat(p.comNome(null)).isSameAs(p);
        assertThat(p.comNome("  ")).isSameAs(p);
        assertThat(p.comNome("João P.")).isEqualTo(new Paciente("pac-101", "João P.", "11111111111", "700000000000001"));
        assertThat(Paciente.novo("pac-9", "Ana")).isEqualTo(new Paciente("pac-9", "Ana", null, null));
    }

    @Test
    void eventoCarregaSomenteDadosNaoPessoais() {
        RegistroFila r = RegistroFila.avisar("pac-101", "upa-01", List.of("febre"), null, Prioridade.URGENTE, T0);
        r.confirmarChegada(T0.plusMinutes(3));

        EventoDominioFila e = EventoDominioFila.de(EventoFila.PACIENTE_CHEGOU, r, StatusFila.A_CAMINHO, T0.plusMinutes(3));

        assertThat(e.eventoId()).isNotNull();
        assertThat(e.tipo()).isEqualTo(EventoFila.PACIENTE_CHEGOU);
        assertThat(e.registroId()).isEqualTo(r.getId());
        assertThat(e.unidadeId()).isEqualTo("upa-01");
        assertThat(e.statusAnterior()).isEqualTo(StatusFila.A_CAMINHO);
        assertThat(e.statusAtual()).isEqualTo(StatusFila.AGUARDANDO);
        assertThat(e.prioridade()).isEqualTo(Prioridade.URGENTE);
        assertThat(e.ocorridoEm()).isEqualTo(T0.plusMinutes(3));
    }

    @Test
    void recursoNaoEncontradoInformaRecursoEId() {
        assertThat(new RecursoNaoEncontradoException("Unidade", "upa-99")).hasMessage("Unidade não encontrado(a): upa-99");
    }
}
