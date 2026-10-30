package br.com.openprontuario.fila.api.unidade;

import br.com.openprontuario.fila.application.unidade.DadosUnidadeCommand;
import br.com.openprontuario.fila.application.unidade.atualizarunidade.AtualizarUnidadeUseCase;
import br.com.openprontuario.fila.application.unidade.buscarunidade.BuscarUnidadeUseCase;
import br.com.openprontuario.fila.application.unidade.criarunidade.CriarUnidadeUseCase;
import br.com.openprontuario.fila.application.unidade.listarminhas.ListarMinhasUnidadesUseCase;
import br.com.openprontuario.fila.application.unidade.listarproximas.ListarUnidadesProximasUseCase;
import br.com.openprontuario.fila.application.unidade.listarproximas.UnidadeProximaResult;
import br.com.openprontuario.fila.application.unidade.listarunidades.ListarUnidadesUseCase;
import br.com.openprontuario.fila.application.unidade.removerunidade.RemoverUnidadeUseCase;
import br.com.openprontuario.fila.bootstrap.OpenProntuarioFilaApplication;
import br.com.openprontuario.fila.domain.exception.RecursoNaoEncontradoException;
import br.com.openprontuario.fila.domain.exception.UnidadeEmUsoException;
import br.com.openprontuario.fila.domain.model.TipoUnidade;
import br.com.openprontuario.fila.domain.model.UnidadeSaude;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.List;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = UnidadeController.class, properties = "INTERNAL_API_KEY=chave-teste")
@ContextConfiguration(classes = OpenProntuarioFilaApplication.class)
class UnidadeControllerTest {

    static final UnidadeSaude UPA_01 = new UnidadeSaude("upa-01", "UPA Boa Vista", TipoUnidade.UPA, -25.3866, -49.2346);

    @Autowired MockMvc mvc;
    @MockitoBean ListarUnidadesUseCase listar;
    @MockitoBean ListarUnidadesProximasUseCase proximas;
    @MockitoBean ListarMinhasUnidadesUseCase minhas;
    @MockitoBean BuscarUnidadeUseCase buscar;
    @MockitoBean CriarUnidadeUseCase criar;
    @MockitoBean AtualizarUnidadeUseCase atualizar;
    @MockitoBean RemoverUnidadeUseCase remover;

    static MockHttpServletRequestBuilder comoBff(MockHttpServletRequestBuilder req) {
        return req.header("X-Internal-Key", "chave-teste").header("X-User-Id", "usr-gestor-01")
                .header("X-User-Roles", "GESTOR");
    }

    @Test
    void listarComFiltroDeTipo() throws Exception {
        when(listar.executar(TipoUnidade.UPA)).thenReturn(List.of(UPA_01));
        mvc.perform(comoBff(get("/unidades").param("tipo", "UPA")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("upa-01"))
                .andExpect(jsonPath("$[0].latitude").value(-25.3866));
    }

    @Test
    void proximasComDistanciaArredondada() throws Exception {
        when(proximas.executar(-25.44, -49.22, 20)).thenReturn(List.of(new UnidadeProximaResult(UPA_01, 6.12345)));
        mvc.perform(comoBff(get("/unidades/proximas").param("latitude", "-25.44").param("longitude", "-49.22")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].distanciaKm").value(6.12))
                .andExpect(jsonPath("$[0].nome").value("UPA Boa Vista"));
    }

    @Test
    void proximasValidaParametros() throws Exception {
        mvc.perform(comoBff(get("/unidades/proximas").param("latitude", "-95").param("longitude", "0")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("REQUISICAO_INVALIDA"));
        mvc.perform(comoBff(get("/unidades/proximas").param("latitude", "0").param("longitude", "0")
                        .param("raioKm", "500")))
                .andExpect(status().isBadRequest());
        mvc.perform(comoBff(get("/unidades/proximas").param("longitude", "0")))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(proximas);
    }

    @Test
    void minhasUsaOUsuarioDoCabecalho() throws Exception {
        when(minhas.executar("usr-gestor-01")).thenReturn(List.of(UPA_01));
        mvc.perform(comoBff(get("/unidades/minhas"))).andExpect(status().isOk()).andExpect(jsonPath("$[0].id").value("upa-01"));
    }

    @Test
    void buscarOu404() throws Exception {
        when(buscar.executar("upa-01")).thenReturn(UPA_01);
        when(buscar.executar("x")).thenThrow(new RecursoNaoEncontradoException("Unidade", "x"));
        mvc.perform(comoBff(get("/unidades/upa-01"))).andExpect(status().isOk()).andExpect(jsonPath("$.tipo").value("UPA"));
        mvc.perform(comoBff(get("/unidades/x"))).andExpect(status().isNotFound())
                .andExpect(jsonPath("$.codigo").value("RECURSO_NAO_ENCONTRADO"));
    }

    @Test
    void criarAtualizarRemover() throws Exception {
        DadosUnidadeCommand dados = new DadosUnidadeCommand("UPA Boa Vista", TipoUnidade.UPA, -25.3866, -49.2346);
        when(criar.executar(dados)).thenReturn(UPA_01);
        when(atualizar.executar("upa-01", dados)).thenReturn(UPA_01);
        String corpo = "{\"nome\":\"UPA Boa Vista\",\"tipo\":\"UPA\",\"latitude\":-25.3866,\"longitude\":-49.2346}";

        mvc.perform(comoBff(post("/unidades")).contentType(MediaType.APPLICATION_JSON).content(corpo))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.id").value("upa-01"));
        mvc.perform(comoBff(put("/unidades/upa-01")).contentType(MediaType.APPLICATION_JSON).content(corpo))
                .andExpect(status().isOk());
        mvc.perform(comoBff(delete("/unidades/ubs-03"))).andExpect(status().isNoContent());
        verify(remover).executar("ubs-03");
    }

    @Test
    void criarValidaCorpo() throws Exception {
        mvc.perform(comoBff(post("/unidades")).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"U\",\"tipo\":\"UPA\",\"latitude\":200}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value(org.hamcrest.Matchers.containsString("latitude")));
        verifyNoInteractions(criar);
    }

    @Test
    void removerEmUsoEh409() throws Exception {
        doThrow(new UnidadeEmUsoException("em uso")).when(remover).executar("upa-01");
        mvc.perform(comoBff(delete("/unidades/upa-01"))).andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value("UNIDADE_EM_USO"));
    }
}
