package br.com.openprontuario.fila.api.lotacao;

import br.com.openprontuario.fila.api.fila.FilaInternaController;
import br.com.openprontuario.fila.application.fila.snapshot.ObterSnapshotFilaUseCase;
import br.com.openprontuario.fila.application.lotacao.criarlotacao.CriarLotacaoCommand;
import br.com.openprontuario.fila.application.lotacao.criarlotacao.CriarLotacaoUseCase;
import br.com.openprontuario.fila.application.lotacao.listarlotacoes.ListarLotacoesUseCase;
import br.com.openprontuario.fila.application.lotacao.listarlotacoesdousuario.ListarLotacoesDoUsuarioUseCase;
import br.com.openprontuario.fila.application.lotacao.removerlotacao.RemoverLotacaoUseCase;
import br.com.openprontuario.fila.application.lotacao.verificarlotacao.VerificarLotacaoUseCase;
import br.com.openprontuario.fila.bootstrap.OpenProntuarioFilaApplication;
import br.com.openprontuario.fila.domain.exception.LotacaoDuplicadaException;
import br.com.openprontuario.fila.domain.model.Lotacao;
import br.com.openprontuario.fila.domain.model.PerfilUsuario;
import br.com.openprontuario.fila.domain.model.Prioridade;
import br.com.openprontuario.fila.domain.model.RegistroFila;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.OffsetDateTime;
import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Lotações (CRUD do gestor) e as rotas /internal/** usadas pela Function e pelo MS Prontuário. */
@WebMvcTest(controllers = {LotacaoController.class, LotacaoInternaController.class, FilaInternaController.class},
        properties = "INTERNAL_API_KEY=chave-teste")
@ContextConfiguration(classes = OpenProntuarioFilaApplication.class)
class LotacaoControllersTest {

    @Autowired MockMvc mvc;
    @MockitoBean ListarLotacoesUseCase listar;
    @MockitoBean CriarLotacaoUseCase criar;
    @MockitoBean RemoverLotacaoUseCase remover;
    @MockitoBean ListarLotacoesDoUsuarioUseCase listarDoUsuario;
    @MockitoBean VerificarLotacaoUseCase verificar;
    @MockitoBean ObterSnapshotFilaUseCase snapshot;

    private final Lotacao lot1 = new Lotacao("lot-1", "usr-atendente-07", "upa-01", PerfilUsuario.ATENDENTE);

    @Test
    void crudDeLotacoes() throws Exception {
        when(listar.executar("upa-01")).thenReturn(List.of(lot1));
        when(criar.executar(new CriarLotacaoCommand("usr-atendente-07", "upa-01", PerfilUsuario.ATENDENTE)))
                .thenReturn(lot1);

        mvc.perform(get("/lotacoes").param("unidadeId", "upa-01")
                        .header("X-Internal-Key", "chave-teste").header("X-User-Id", "usr-gestor-01"))
                .andExpect(status().isOk()).andExpect(jsonPath("$[0].perfil").value("ATENDENTE"));
        mvc.perform(post("/lotacoes").header("X-Internal-Key", "chave-teste").header("X-User-Id", "usr-gestor-01")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"usuarioId\":\"usr-atendente-07\",\"unidadeId\":\"upa-01\",\"perfil\":\"ATENDENTE\"}"))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.id").value("lot-1"));
        mvc.perform(delete("/lotacoes/lot-1").header("X-Internal-Key", "chave-teste").header("X-User-Id", "g"))
                .andExpect(status().isNoContent());
        verify(remover).executar("lot-1");
    }

    @Test
    void lotacaoDuplicadaEh409() throws Exception {
        when(criar.executar(new CriarLotacaoCommand("u", "upa-01", PerfilUsuario.GESTOR)))
                .thenThrow(new LotacaoDuplicadaException("dup"));
        mvc.perform(post("/lotacoes").header("X-Internal-Key", "chave-teste").header("X-User-Id", "g")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"usuarioId\":\"u\",\"unidadeId\":\"upa-01\",\"perfil\":\"GESTOR\"}"))
                .andExpect(status().isConflict()).andExpect(jsonPath("$.codigo").value("LOTACAO_DUPLICADA"));
    }

    @Test
    void rotasInternasExigemSoAChaveInterna() throws Exception {
        when(listarDoUsuario.executar("usr-atendente-07")).thenReturn(List.of(lot1));
        when(verificar.executar("usr-atendente-07", "upa-01")).thenReturn(true);

        mvc.perform(get("/internal/lotacoes").param("usuarioId", "usr-atendente-07").header("X-Internal-Key", "chave-teste"))
                .andExpect(status().isOk()).andExpect(jsonPath("$[0].unidadeId").value("upa-01"));
        mvc.perform(get("/internal/lotacoes/verificar").param("usuarioId", "usr-atendente-07")
                        .param("unidadeId", "upa-01").header("X-Internal-Key", "chave-teste"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.lotado").value(true));
        mvc.perform(get("/internal/lotacoes").param("usuarioId", "x"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void snapshotSemDadosPessoais() throws Exception {
        RegistroFila r = RegistroFila.avisar("pac-101", "upa-01", List.of("febre"), null, Prioridade.URGENTE,
                OffsetDateTime.parse("2026-10-14T08:05:00-03:00"));
        when(snapshot.executar("upa-01")).thenReturn(List.of(r));

        mvc.perform(get("/internal/fila/upa-01/snapshot").header("X-Internal-Key", "chave-teste"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(r.getId()))
                .andExpect(jsonPath("$[0].status").value("A_CAMINHO"))
                .andExpect(jsonPath("$[0].pacienteId").doesNotExist())
                .andExpect(jsonPath("$[0].sintomas").doesNotExist());
    }
}
