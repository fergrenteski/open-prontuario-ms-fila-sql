package br.com.openprontuario.fila.infrastructure.persistence;

import br.com.openprontuario.fila.domain.exception.LotacaoDuplicadaException;
import br.com.openprontuario.fila.domain.exception.RegistroAtivoDuplicadoException;
import br.com.openprontuario.fila.domain.exception.UnidadeEmUsoException;
import br.com.openprontuario.fila.domain.model.Lotacao;
import br.com.openprontuario.fila.domain.model.MotivoSaida;
import br.com.openprontuario.fila.domain.model.Paciente;
import br.com.openprontuario.fila.domain.model.PerfilUsuario;
import br.com.openprontuario.fila.domain.model.Prioridade;
import br.com.openprontuario.fila.domain.model.RegistroFila;
import br.com.openprontuario.fila.domain.model.StatusFila;
import br.com.openprontuario.fila.domain.model.TipoUnidade;
import br.com.openprontuario.fila.domain.model.UnidadeSaude;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PersistenciaIT extends IntegracaoBaseIT {

    static final OffsetDateTime T0 = OffsetDateTime.parse("2026-10-14T08:05:00.1234567-03:00");

    @Autowired JdbcTemplate jdbc;
    @Autowired UnidadeSaudeRepositoryAdapter unidades;
    @Autowired LotacaoRepositoryAdapter lotacoes;
    @Autowired PacienteRepositoryAdapter pacientes;
    @Autowired RegistroFilaRepositoryAdapter registros;

    private UnidadeSaude novaUnidade() {
        return unidades.salvar(UnidadeSaude.nova("UBS Teste " + UUID.randomUUID(), TipoUnidade.UBS, -25.4, -49.3));
    }

    private Paciente novoPaciente() {
        return pacientes.salvar(Paciente.novo("pac-" + UUID.randomUUID(), "Paciente Teste"));
    }

    @Test
    void flywayAplicouSchemaESeed() {
        assertThat(jdbc.queryForList("select version from flyway_schema_history where success = 1 order by installed_rank",
                String.class)).containsExactly("1", "2");
        assertThat(unidades.buscarPorId("ubs-03")).get()
                .isEqualTo(new UnidadeSaude("ubs-03", "UBS Mãe Curitibana", TipoUnidade.UBS, -25.4372, -49.2700));
        assertThat(unidades.listar(TipoUnidade.UPA)).extracting(UnidadeSaude::id).contains("upa-01", "upa-02");
        assertThat(lotacoes.listarPorUsuario("usr-gestor-01")).extracting(Lotacao::id)
                .containsExactlyInAnyOrder("lot-4", "lot-5", "lot-6");
        assertThat(lotacoes.buscarPorId("lot-3")).get()
                .isEqualTo(new Lotacao("lot-3", "usr-prof-03", "upa-01", PerfilUsuario.PROFISSIONAL));
        assertThat(pacientes.buscarPorId("pac-101")).get()
                .isEqualTo(new Paciente("pac-101", "João Pereira", "11111111111", "700000000000001"));
        assertThat(jdbc.queryForObject("select count(*) from registros_fila where paciente_id in ('pac-101','pac-102')",
                Integer.class)).isZero();
    }

    @Test
    void unidadesDoUsuarioLotadoEVerificacaoDeLotacao() {
        assertThat(unidades.listarPorUsuarioLotado("usr-atendente-07")).extracting(UnidadeSaude::id)
                .containsExactlyInAnyOrder("upa-01", "upa-02");
        assertThat(lotacoes.estaLotado("usr-prof-03", "upa-01")).isTrue();
        assertThat(lotacoes.estaLotado("usr-prof-03", "upa-02")).isFalse();
        assertThat(lotacoes.listarPorUnidade("upa-02")).extracting(Lotacao::usuarioId)
                .contains("usr-atendente-07", "usr-gestor-01");
    }

    @Test
    void crudDeUnidadeEBloqueioDeRemocaoEmUso() {
        UnidadeSaude u = novaUnidade();
        UnidadeSaude atualizada = unidades.salvar(u.atualizada("UPA Renomeada", TipoUnidade.UPA, -25.5, -49.1));
        assertThat(unidades.buscarPorId(u.id())).contains(atualizada);
        assertThat(unidades.emUso(u.id())).isFalse();

        Lotacao l = lotacoes.salvar(Lotacao.nova("usr-x", u.id(), PerfilUsuario.ATENDENTE));
        assertThat(unidades.emUso(u.id())).isTrue();
        assertThatThrownBy(() -> unidades.remover(u.id())).isInstanceOf(UnidadeEmUsoException.class);

        lotacoes.remover(l.id());
        unidades.remover(u.id());
        assertThat(unidades.buscarPorId(u.id())).isEmpty();
    }

    @Test
    void lotacaoDuplicadaViolaIndiceUnico() {
        assertThatThrownBy(() -> lotacoes.salvar(Lotacao.nova("usr-prof-03", "upa-01", PerfilUsuario.PROFISSIONAL)))
                .isInstanceOf(LotacaoDuplicadaException.class);
    }

    @Test
    void registroFazIdaEVoltaPreservandoOffsetESintomas() {
        UnidadeSaude u = novaUnidade();
        Paciente p = novoPaciente();
        RegistroFila r = RegistroFila.avisar(p.id(), u.id(), List.of("Dor no peito", "Tontura"), 15,
                Prioridade.MUITO_URGENTE, T0);
        registros.salvar(r);
        r.confirmarChegada(T0.plusMinutes(12));
        registros.salvar(r);

        RegistroFila lido = registros.buscarPorId(r.getId()).orElseThrow();
        assertThat(lido.getStatus()).isEqualTo(StatusFila.AGUARDANDO);
        assertThat(lido.getSintomas()).containsExactly("Dor no peito", "Tontura");
        assertThat(lido.getHorarioAviso()).isEqualTo(T0);
        assertThat(lido.getHorarioChegada()).isEqualTo(T0.plusMinutes(12));
        assertThat(lido.getTempoEstimadoChegadaMin()).isEqualTo(15);
        assertThat(pacientes.buscarNomes(List.of(p.id(), "inexistente"))).containsOnlyKeys(p.id());
    }

    @Test
    void indiceUnicoFiltradoPermiteUmRegistroAtivoPorPacienteEUnidade() {
        UnidadeSaude u = novaUnidade();
        Paciente p = novoPaciente();
        RegistroFila primeiro = RegistroFila.avisar(p.id(), u.id(), List.of("febre"), null, Prioridade.POUCO_URGENTE, T0);
        registros.salvar(primeiro);
        assertThat(registros.existeAtivo(p.id(), u.id())).isTrue();

        RegistroFila duplicado = RegistroFila.avisar(p.id(), u.id(), List.of("febre"), null, Prioridade.POUCO_URGENTE, T0);
        assertThatThrownBy(() -> registros.salvar(duplicado)).isInstanceOf(RegistroAtivoDuplicadoException.class);

        primeiro.encerrar(MotivoSaida.DESISTENCIA, T0.plusMinutes(1));
        registros.salvar(primeiro);
        assertThat(registros.existeAtivo(p.id(), u.id())).isFalse();
        registros.salvar(duplicado);   // finalizado não conta: novo aviso é aceito
        assertThat(registros.existeAtivo(p.id(), u.id())).isTrue();
    }

    @Test
    void ativosESnapshotDaUnidade() {
        UnidadeSaude u = novaUnidade();
        OffsetDateTime agora = OffsetDateTime.now();
        RegistroFila aCaminho = RegistroFila.avisar(novoPaciente().id(), u.id(), List.of("x"), null, Prioridade.URGENTE, agora);
        RegistroFila atendidoAgora = RegistroFila.avisar(novoPaciente().id(), u.id(), List.of("x"), null, Prioridade.URGENTE, agora.minusMinutes(40));
        atendidoAgora.confirmarChegada(agora.minusMinutes(30));
        atendidoAgora.iniciarAtendimento(agora.minusMinutes(10));
        atendidoAgora.encerrar(MotivoSaida.ATENDIDO, agora);
        RegistroFila atendidoOntem = RegistroFila.avisar(novoPaciente().id(), u.id(), List.of("x"), null, Prioridade.URGENTE, agora.minusDays(1));
        atendidoOntem.confirmarChegada(agora.minusDays(1));
        atendidoOntem.iniciarAtendimento(agora.minusDays(1));
        atendidoOntem.encerrar(MotivoSaida.ATENDIDO, agora.minusDays(1));
        List.of(aCaminho, atendidoAgora, atendidoOntem).forEach(registros::salvar);

        assertThat(registros.listarAtivosPorUnidade(u.id())).extracting(RegistroFila::getId)
                .containsExactly(aCaminho.getId());
        assertThat(registros.listarParaSnapshot(u.id(), agora.minusHours(2))).extracting(RegistroFila::getId)
                .containsExactlyInAnyOrder(aCaminho.getId(), atendidoAgora.getId());
    }
}
