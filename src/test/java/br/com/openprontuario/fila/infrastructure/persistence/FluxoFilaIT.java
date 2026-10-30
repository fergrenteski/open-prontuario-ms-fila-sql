package br.com.openprontuario.fila.infrastructure.persistence;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Fluxo completo HTTP → casos de uso → SQL Server real: aviso, duplicidade, fila, ações e snapshot. */
@AutoConfigureMockMvc
class FluxoFilaIT extends IntegracaoBaseIT {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;

    private static MockHttpServletRequestBuilder como(MockHttpServletRequestBuilder req, String usuario) {
        return req.header("X-Internal-Key", "chave-it").header("X-User-Id", usuario);
    }

    @Test
    void pacienteAvisaAtendenteConduzAteFinalizar() throws Exception {
        String corpo = mvc.perform(como(post("/fila/upa-02/aviso-chegada"), "pac-102")
                        .header("X-User-Name", "Maria Souza").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"sintomas\":[\"Falta de ar\"],\"tempoEstimadoChegadaMin\":10}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.prioridade").value("MUITO_URGENTE"))
                .andExpect(jsonPath("$.pacienteNome").value("Maria Souza"))
                .andReturn().getResponse().getContentAsString();
        String id = json.readTree(corpo).get("id").asText();

        mvc.perform(como(post("/fila/upa-02/aviso-chegada"), "pac-102").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"sintomas\":[\"febre\"]}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value("REGISTRO_ATIVO_DUPLICADO"));

        // usr-prof-03 só é lotado na upa-01
        mvc.perform(como(post("/fila/" + id + "/confirmar-chegada"), "usr-prof-03"))
                .andExpect(status().isForbidden());
        mvc.perform(como(post("/fila/" + id + "/iniciar-atendimento"), "usr-atendente-07"))
                .andExpect(status().isConflict()).andExpect(jsonPath("$.codigo").value("TRANSICAO_INVALIDA"));
        mvc.perform(como(post("/fila/" + id + "/confirmar-chegada"), "usr-atendente-07"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("AGUARDANDO"));

        JsonNode fila = json.readTree(mvc.perform(como(get("/fila/upa-02"), "usr-atendente-07"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString());
        org.assertj.core.api.Assertions.assertThat(fila.findValuesAsText("id")).contains(id);

        mvc.perform(como(post("/fila/" + id + "/iniciar-atendimento"), "usr-atendente-07"))
                .andExpect(status().isOk());
        mvc.perform(como(delete("/fila/" + id).param("motivo", "ATENDIDO"), "usr-atendente-07"))
                .andExpect(status().isNoContent());

        // finalizado há instantes ainda entra no snapshot (início de atendimento nas últimas 2 h)
        mvc.perform(get("/internal/fila/upa-02/snapshot").header("X-Internal-Key", "chave-it"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.id == '" + id + "')].status").value("FINALIZADO"));
        mvc.perform(get("/actuator/health")).andExpect(status().isOk());
    }
}
