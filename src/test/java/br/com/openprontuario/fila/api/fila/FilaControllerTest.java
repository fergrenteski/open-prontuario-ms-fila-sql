package br.com.openprontuario.fila.api.fila;

import br.com.openprontuario.fila.application.fila.RegistroFilaResult;
import br.com.openprontuario.fila.application.fila.alterarprioridade.AlterarPrioridadeUseCase;
import br.com.openprontuario.fila.application.fila.avisarchegada.AvisarChegadaCommand;
import br.com.openprontuario.fila.application.fila.avisarchegada.AvisarChegadaUseCase;
import br.com.openprontuario.fila.application.fila.confirmarchegada.ConfirmarChegadaUseCase;
import br.com.openprontuario.fila.application.fila.iniciaratendimento.IniciarAtendimentoUseCase;
import br.com.openprontuario.fila.application.fila.listarfila.ListarFilaPorPrioridadeUseCase;
import br.com.openprontuario.fila.application.fila.removerdafila.RemoverDaFilaUseCase;
import br.com.openprontuario.fila.bootstrap.OpenProntuarioFilaApplication;
import br.com.openprontuario.fila.domain.exception.AcessoNegadoException;
import br.com.openprontuario.fila.domain.exception.RecursoNaoEncontradoException;
import br.com.openprontuario.fila.domain.exception.RegistroAtivoDuplicadoException;
import br.com.openprontuario.fila.domain.exception.TransicaoInvalidaException;
import br.com.openprontuario.fila.domain.model.MotivoSaida;
import br.com.openprontuario.fila.domain.model.Prioridade;
import br.com.openprontuario.fila.domain.model.RegistroFila;
import br.com.openprontuario.fila.domain.model.StatusFila;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.time.OffsetDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = FilaController.class, properties = "INTERNAL_API_KEY=chave-teste")
@ContextConfiguration(classes = OpenProntuarioFilaApplication.class)
class FilaControllerTest {

    static final OffsetDateTime T0 = OffsetDateTime.parse("2026-10-14T08:05:00-03:00");

    @Autowired MockMvc mvc;
    @MockitoBean ListarFilaPorPrioridadeUseCase listar;
    @MockitoBean AvisarChegadaUseCase avisar;
    @MockitoBean ConfirmarChegadaUseCase confirmar;
    @MockitoBean AlterarPrioridadeUseCase alterarPrioridade;
    @MockitoBean IniciarAtendimentoUseCase iniciar;
    @MockitoBean RemoverDaFilaUseCase remover;

    /** Requisição como o BFF envia: chave interna + identidade do usuário. */
    static MockHttpServletRequestBuilder comoBff(MockHttpServletRequestBuilder req, String usuario) {
        return req.header("X-Internal-Key", "chave-teste").header("X-User-Id", usuario)
                .header("X-User-Roles", "ATENDENTE");
    }

    private final RegistroFila registro = RegistroFila.avisar("pac-101", "upa-01", List.of("dor no peito"), 15,
            Prioridade.MUITO_URGENTE, T0);

    @Test
    void avisoDeChegadaCria201ComPacienteDoCabecalho() throws Exception {
        when(avisar.executar(any())).thenReturn(new RegistroFilaResult(registro, "João Pereira"));

        mvc.perform(comoBff(post("/fila/upa-01/aviso-chegada"), "pac-101").header("X-User-Name", "João Pereira")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"sintomas\":[\"dor no peito\"],\"tempoEstimadoChegadaMin\":15}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(registro.getId()))
                .andExpect(jsonPath("$.pacienteId").value("pac-101"))
                .andExpect(jsonPath("$.pacienteNome").value("João Pereira"))
                .andExpect(jsonPath("$.status").value("A_CAMINHO"))
                .andExpect(jsonPath("$.prioridade").value("MUITO_URGENTE"))
                .andExpect(jsonPath("$.horarioAviso").value("2026-10-14T08:05:00-03:00"))
                .andExpect(jsonPath("$.horarioChegada").doesNotExist());

        verify(avisar).executar(new AvisarChegadaCommand("pac-101", "João Pereira", "upa-01",
                List.of("dor no peito"), 15));
    }

    @Test
    void nomeComAcentoChegaCertoEmUtf8OuLatin1() throws Exception {
        when(avisar.executar(any())).thenReturn(new RegistroFilaResult(registro, "João Pereira"));
        String utf8LidoComoLatin1 = new String("João Pereira".getBytes(java.nio.charset.StandardCharsets.UTF_8),
                java.nio.charset.StandardCharsets.ISO_8859_1);

        for (String cabecalho : List.of(utf8LidoComoLatin1, "João Pereira")) {
            mvc.perform(comoBff(post("/fila/upa-01/aviso-chegada"), "pac-101").header("X-User-Name", cabecalho)
                            .contentType(MediaType.APPLICATION_JSON).content("{\"sintomas\":[\"febre\"]}"))
                    .andExpect(status().isCreated());
        }
        verify(avisar, org.mockito.Mockito.times(2)).executar(new AvisarChegadaCommand("pac-101", "João Pereira",
                "upa-01", List.of("febre"), null));
    }

    @Test
    void avisoSemSintomasEh400ComProblemDetails() throws Exception {
        mvc.perform(comoBff(post("/fila/upa-01/aviso-chegada"), "pac-101").header("X-Correlation-Id", "corr-123")
                        .contentType(MediaType.APPLICATION_JSON).content("{\"sintomas\":[]}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.codigo").value("REQUISICAO_INVALIDA"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.correlationId").value("corr-123"))
                .andExpect(jsonPath("$.instance").value("/fila/upa-01/aviso-chegada"))
                .andExpect(header().string("X-Correlation-Id", "corr-123"));
        verifyNoInteractions(avisar);
    }

    @Test
    void jsonMalformadoEh400() throws Exception {
        mvc.perform(comoBff(post("/fila/upa-01/aviso-chegada"), "pac-101")
                        .contentType(MediaType.APPLICATION_JSON).content("{sintomas"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("REQUISICAO_INVALIDA"));
    }

    @Test
    void registroAtivoDuplicadoEh409() throws Exception {
        when(avisar.executar(any())).thenThrow(new RegistroAtivoDuplicadoException("duplicado"));

        mvc.perform(comoBff(post("/fila/upa-01/aviso-chegada"), "pac-101")
                        .contentType(MediaType.APPLICATION_JSON).content("{\"sintomas\":[\"febre\"]}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value("REGISTRO_ATIVO_DUPLICADO"))
                .andExpect(jsonPath("$.type").value("https://openprontuario.dev/erros/registro-ativo-duplicado"));
    }

    @Test
    void listarFilaUsaIdComoUnidadeEFiltroDeStatus() throws Exception {
        when(listar.executar("upa-01", "usr-atendente-07", StatusFila.A_CAMINHO))
                .thenReturn(List.of(new RegistroFilaResult(registro, null)));

        mvc.perform(comoBff(get("/fila/upa-01").param("status", "A_CAMINHO"), "usr-atendente-07"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(registro.getId()))
                .andExpect(jsonPath("$[0].sintomas[0]").value("dor no peito"));
    }

    @Test
    void naoLotadoEh403() throws Exception {
        when(listar.executar(anyString(), anyString(), any())).thenThrow(new AcessoNegadoException("não lotado"));

        mvc.perform(comoBff(get("/fila/upa-02"), "usr-prof-03"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.codigo").value("SEM_PERMISSAO"));
    }

    @Test
    void acoesDoAtendente() throws Exception {
        RegistroFilaResult r = new RegistroFilaResult(registro, "João Pereira");
        when(confirmar.executar("r1", "usr-atendente-07")).thenReturn(r);
        when(iniciar.executar("r1", "usr-atendente-07")).thenReturn(r);
        when(alterarPrioridade.executar("r1", Prioridade.EMERGENCIA, "usr-atendente-07")).thenReturn(r);

        mvc.perform(comoBff(post("/fila/r1/confirmar-chegada"), "usr-atendente-07")).andExpect(status().isOk());
        mvc.perform(comoBff(post("/fila/r1/iniciar-atendimento"), "usr-atendente-07")).andExpect(status().isOk());
        mvc.perform(comoBff(patch("/fila/r1/prioridade"), "usr-atendente-07").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"prioridade\":\"EMERGENCIA\",\"justificativa\":\"triagem\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pacienteNome").value("João Pereira"));
        mvc.perform(comoBff(delete("/fila/r1").param("motivo", "ATENDIDO"), "usr-atendente-07"))
                .andExpect(status().isNoContent());

        verify(remover).executar("r1", MotivoSaida.ATENDIDO, "usr-atendente-07");
    }

    @Test
    void prioridadeInvalidaEMotivoAusenteSao400() throws Exception {
        mvc.perform(comoBff(patch("/fila/r1/prioridade"), "u").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"prioridade\":\"GRAVISSIMO\"}"))
                .andExpect(status().isBadRequest());
        mvc.perform(comoBff(delete("/fila/r1"), "u"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("REQUISICAO_INVALIDA"));
        mvc.perform(comoBff(delete("/fila/r1").param("motivo", "FUGIU"), "u"))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(remover, alterarPrioridade);
    }

    @Test
    void transicaoInvalidaEh409ERegistroInexistenteEh404() throws Exception {
        when(iniciar.executar(eq("r1"), anyString())).thenThrow(new TransicaoInvalidaException("A_CAMINHO -> EM_ATENDIMENTO"));
        when(confirmar.executar(eq("x"), anyString())).thenThrow(new RecursoNaoEncontradoException("Registro de fila", "x"));

        mvc.perform(comoBff(post("/fila/r1/iniciar-atendimento"), "u"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value("TRANSICAO_INVALIDA"));
        mvc.perform(comoBff(post("/fila/x/confirmar-chegada"), "u"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.codigo").value("RECURSO_NAO_ENCONTRADO"));
    }

    @Test
    void semChaveInternaOuComChaveErradaEh401() throws Exception {
        mvc.perform(get("/fila/upa-01").header("X-User-Id", "u"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.codigo").value("NAO_AUTENTICADO"))
                .andExpect(jsonPath("$.correlationId").isNotEmpty())
                .andExpect(header().exists("X-Correlation-Id"));
        mvc.perform(get("/fila/upa-01").header("X-User-Id", "u").header("X-Internal-Key", "chave-errada"))
                .andExpect(status().isUnauthorized());
        verifyNoInteractions(listar);
    }

    @Test
    void semUsuarioNaRotaDeUsuarioEh401() throws Exception {
        mvc.perform(get("/fila/upa-01").header("X-Internal-Key", "chave-teste"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.detail").value("Cabeçalho X-User-Id ausente"));
    }

    @Test
    void correlationIdInseguroEhSubstituido() throws Exception {
        when(listar.executar(anyString(), anyString(), any())).thenReturn(List.of());

        mvc.perform(comoBff(get("/fila/upa-01"), "u").header("X-Correlation-Id", "abc\r\nINJECAO"))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Correlation-Id",
                        org.hamcrest.Matchers.matchesPattern("[0-9a-f-]{36}")));
    }

    @Test
    void erroInesperadoEh500SemDetalhesInternos() throws Exception {
        when(listar.executar(anyString(), anyString(), any())).thenThrow(new IllegalStateException("SELECT * FROM segredo"));

        mvc.perform(comoBff(get("/fila/upa-01"), "u"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.codigo").value("ERRO_INTERNO"))
                .andExpect(jsonPath("$.detail").value(org.hamcrest.Matchers.not(
                        org.hamcrest.Matchers.containsString("SELECT"))));
    }

    @Test
    void falhaDeBancoEh503() throws Exception {
        when(listar.executar(anyString(), anyString(), any()))
                .thenThrow(new org.springframework.dao.DataAccessResourceFailureException("conexão recusada"));

        mvc.perform(comoBff(get("/fila/upa-01"), "u"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.codigo").value("DEPENDENCIA_INDISPONIVEL"));
    }
}
