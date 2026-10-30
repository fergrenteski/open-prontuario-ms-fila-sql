package br.com.openprontuario.fila.bootstrap;

import br.com.openprontuario.fila.application.fila.alterarprioridade.AlterarPrioridadeUseCase;
import br.com.openprontuario.fila.application.fila.avisarchegada.AvisarChegadaUseCase;
import br.com.openprontuario.fila.application.fila.confirmarchegada.ConfirmarChegadaUseCase;
import br.com.openprontuario.fila.application.fila.iniciaratendimento.IniciarAtendimentoUseCase;
import br.com.openprontuario.fila.application.fila.listarfila.ListarFilaPorPrioridadeUseCase;
import br.com.openprontuario.fila.application.fila.ports.EventPublisherPort;
import br.com.openprontuario.fila.application.fila.ports.PacienteRepository;
import br.com.openprontuario.fila.application.fila.ports.RegistroFilaRepository;
import br.com.openprontuario.fila.application.fila.removerdafila.RemoverDaFilaUseCase;
import br.com.openprontuario.fila.application.fila.snapshot.ObterSnapshotFilaUseCase;
import br.com.openprontuario.fila.application.lotacao.criarlotacao.CriarLotacaoUseCase;
import br.com.openprontuario.fila.application.lotacao.listarlotacoes.ListarLotacoesUseCase;
import br.com.openprontuario.fila.application.lotacao.listarlotacoesdousuario.ListarLotacoesDoUsuarioUseCase;
import br.com.openprontuario.fila.application.lotacao.ports.LotacaoRepository;
import br.com.openprontuario.fila.application.lotacao.removerlotacao.RemoverLotacaoUseCase;
import br.com.openprontuario.fila.application.lotacao.verificarlotacao.VerificarLotacaoUseCase;
import br.com.openprontuario.fila.application.unidade.atualizarunidade.AtualizarUnidadeUseCase;
import br.com.openprontuario.fila.application.unidade.buscarunidade.BuscarUnidadeUseCase;
import br.com.openprontuario.fila.application.unidade.criarunidade.CriarUnidadeUseCase;
import br.com.openprontuario.fila.application.unidade.listarminhas.ListarMinhasUnidadesUseCase;
import br.com.openprontuario.fila.application.unidade.listarproximas.ListarUnidadesProximasUseCase;
import br.com.openprontuario.fila.application.unidade.listarunidades.ListarUnidadesUseCase;
import br.com.openprontuario.fila.application.unidade.ports.UnidadeSaudeRepository;
import br.com.openprontuario.fila.application.unidade.removerunidade.RemoverUnidadeUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.ZoneId;

/** Instancia os casos de uso (Java puro, sem anotações) com as portas implementadas na infraestrutura. */
@Configuration
public class UseCaseConfig {

    @Bean
    Clock relogio() {
        return Clock.system(ZoneId.of("America/Sao_Paulo"));
    }

    // Unidades
    @Bean
    ListarUnidadesUseCase listarUnidadesUseCase(UnidadeSaudeRepository unidades) {
        return new ListarUnidadesUseCase(unidades);
    }

    @Bean
    ListarUnidadesProximasUseCase listarUnidadesProximasUseCase(UnidadeSaudeRepository unidades) {
        return new ListarUnidadesProximasUseCase(unidades);
    }

    @Bean
    ListarMinhasUnidadesUseCase listarMinhasUnidadesUseCase(UnidadeSaudeRepository unidades) {
        return new ListarMinhasUnidadesUseCase(unidades);
    }

    @Bean
    BuscarUnidadeUseCase buscarUnidadeUseCase(UnidadeSaudeRepository unidades) {
        return new BuscarUnidadeUseCase(unidades);
    }

    @Bean
    CriarUnidadeUseCase criarUnidadeUseCase(UnidadeSaudeRepository unidades) {
        return new CriarUnidadeUseCase(unidades);
    }

    @Bean
    AtualizarUnidadeUseCase atualizarUnidadeUseCase(UnidadeSaudeRepository unidades) {
        return new AtualizarUnidadeUseCase(unidades);
    }

    @Bean
    RemoverUnidadeUseCase removerUnidadeUseCase(UnidadeSaudeRepository unidades) {
        return new RemoverUnidadeUseCase(unidades);
    }

    // Lotações
    @Bean
    ListarLotacoesUseCase listarLotacoesUseCase(LotacaoRepository lotacoes) {
        return new ListarLotacoesUseCase(lotacoes);
    }

    @Bean
    CriarLotacaoUseCase criarLotacaoUseCase(LotacaoRepository lotacoes, UnidadeSaudeRepository unidades) {
        return new CriarLotacaoUseCase(lotacoes, unidades);
    }

    @Bean
    RemoverLotacaoUseCase removerLotacaoUseCase(LotacaoRepository lotacoes) {
        return new RemoverLotacaoUseCase(lotacoes);
    }

    @Bean
    ListarLotacoesDoUsuarioUseCase listarLotacoesDoUsuarioUseCase(LotacaoRepository lotacoes) {
        return new ListarLotacoesDoUsuarioUseCase(lotacoes);
    }

    @Bean
    VerificarLotacaoUseCase verificarLotacaoUseCase(LotacaoRepository lotacoes) {
        return new VerificarLotacaoUseCase(lotacoes);
    }

    // Fila
    @Bean
    ListarFilaPorPrioridadeUseCase listarFilaPorPrioridadeUseCase(UnidadeSaudeRepository unidades,
            LotacaoRepository lotacoes, RegistroFilaRepository registros, PacienteRepository pacientes) {
        return new ListarFilaPorPrioridadeUseCase(unidades, lotacoes, registros, pacientes);
    }

    @Bean
    AvisarChegadaUseCase avisarChegadaUseCase(UnidadeSaudeRepository unidades, PacienteRepository pacientes,
            RegistroFilaRepository registros, EventPublisherPort eventos, Clock relogio) {
        return new AvisarChegadaUseCase(unidades, pacientes, registros, eventos, relogio);
    }

    @Bean
    ConfirmarChegadaUseCase confirmarChegadaUseCase(RegistroFilaRepository registros, LotacaoRepository lotacoes,
            PacienteRepository pacientes, EventPublisherPort eventos, Clock relogio) {
        return new ConfirmarChegadaUseCase(registros, lotacoes, pacientes, eventos, relogio);
    }

    @Bean
    AlterarPrioridadeUseCase alterarPrioridadeUseCase(RegistroFilaRepository registros, LotacaoRepository lotacoes,
            PacienteRepository pacientes, EventPublisherPort eventos, Clock relogio) {
        return new AlterarPrioridadeUseCase(registros, lotacoes, pacientes, eventos, relogio);
    }

    @Bean
    IniciarAtendimentoUseCase iniciarAtendimentoUseCase(RegistroFilaRepository registros, LotacaoRepository lotacoes,
            PacienteRepository pacientes, EventPublisherPort eventos, Clock relogio) {
        return new IniciarAtendimentoUseCase(registros, lotacoes, pacientes, eventos, relogio);
    }

    @Bean
    RemoverDaFilaUseCase removerDaFilaUseCase(RegistroFilaRepository registros, LotacaoRepository lotacoes,
            PacienteRepository pacientes, EventPublisherPort eventos, Clock relogio) {
        return new RemoverDaFilaUseCase(registros, lotacoes, pacientes, eventos, relogio);
    }

    @Bean
    ObterSnapshotFilaUseCase obterSnapshotFilaUseCase(RegistroFilaRepository registros, Clock relogio) {
        return new ObterSnapshotFilaUseCase(registros, relogio);
    }
}
