package br.com.openprontuario.fila.contrato;

import br.com.openprontuario.fila.api.comum.OpenApiConfig;
import br.com.openprontuario.fila.application.fila.alterarprioridade.AlterarPrioridadeUseCase;
import br.com.openprontuario.fila.application.fila.avisarchegada.AvisarChegadaUseCase;
import br.com.openprontuario.fila.application.fila.confirmarchegada.ConfirmarChegadaUseCase;
import br.com.openprontuario.fila.application.fila.iniciaratendimento.IniciarAtendimentoUseCase;
import br.com.openprontuario.fila.application.fila.listarfila.ListarFilaPorPrioridadeUseCase;
import br.com.openprontuario.fila.application.fila.removerdafila.RemoverDaFilaUseCase;
import br.com.openprontuario.fila.application.fila.snapshot.ObterSnapshotFilaUseCase;
import br.com.openprontuario.fila.application.lotacao.criarlotacao.CriarLotacaoUseCase;
import br.com.openprontuario.fila.application.lotacao.listarlotacoes.ListarLotacoesUseCase;
import br.com.openprontuario.fila.application.lotacao.listarlotacoesdousuario.ListarLotacoesDoUsuarioUseCase;
import br.com.openprontuario.fila.application.lotacao.removerlotacao.RemoverLotacaoUseCase;
import br.com.openprontuario.fila.application.lotacao.verificarlotacao.VerificarLotacaoUseCase;
import br.com.openprontuario.fila.application.unidade.atualizarunidade.AtualizarUnidadeUseCase;
import br.com.openprontuario.fila.application.unidade.buscarunidade.BuscarUnidadeUseCase;
import br.com.openprontuario.fila.application.unidade.criarunidade.CriarUnidadeUseCase;
import br.com.openprontuario.fila.application.unidade.listarminhas.ListarMinhasUnidadesUseCase;
import br.com.openprontuario.fila.application.unidade.listarproximas.ListarUnidadesProximasUseCase;
import br.com.openprontuario.fila.application.unidade.listarunidades.ListarUnidadesUseCase;
import br.com.openprontuario.fila.application.unidade.removerunidade.RemoverUnidadeUseCase;
import br.com.openprontuario.fila.bootstrap.OpenProntuarioFilaApplication;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springdoc.core.configuration.SpringDocConfiguration;
import org.springdoc.core.properties.SpringDocConfigProperties;
import org.springdoc.webmvc.core.configuration.SpringDocWebMvcConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.yaml.snakeyaml.Yaml;

import java.io.FileReader;
import java.io.Reader;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * CONVENCOES §11: as rotas servidas pelo springdoc ({@code /v3/api-docs}) têm de ser exatamente
 * as de {@code contract/ms-fila-sql.openapi.yaml}, com parâmetros de caminho normalizados para {}.
 */
@WebMvcTest(properties = "INTERNAL_API_KEY=chave-teste")
@ContextConfiguration(classes = OpenProntuarioFilaApplication.class)
@ImportAutoConfiguration({SpringDocConfiguration.class, SpringDocConfigProperties.class,
        SpringDocWebMvcConfiguration.class})
@Import(OpenApiConfig.class)
@MockitoBean(types = {ListarUnidadesUseCase.class, ListarUnidadesProximasUseCase.class,
        ListarMinhasUnidadesUseCase.class, BuscarUnidadeUseCase.class, CriarUnidadeUseCase.class,
        AtualizarUnidadeUseCase.class, RemoverUnidadeUseCase.class, ListarLotacoesUseCase.class,
        CriarLotacaoUseCase.class, RemoverLotacaoUseCase.class, ListarLotacoesDoUsuarioUseCase.class,
        VerificarLotacaoUseCase.class, ListarFilaPorPrioridadeUseCase.class, AvisarChegadaUseCase.class,
        ConfirmarChegadaUseCase.class, AlterarPrioridadeUseCase.class, IniciarAtendimentoUseCase.class,
        RemoverDaFilaUseCase.class, ObterSnapshotFilaUseCase.class})
class ContratoOpenApiTest {

    private static final Set<String> METODOS = Set.of("get", "post", "put", "patch", "delete");

    @Autowired MockMvc mvc;

    @Test
    void rotasImplementadasBatemComOContrato() throws Exception {
        Set<String> contrato = rotasDoContrato();
        Set<String> implementado = rotasDoSpringdoc();

        assertThat(contrato).as("contrato vazio?").hasSizeGreaterThan(10);
        assertThat(implementado).as("rotas implementadas e não documentadas no contrato")
                .isSubsetOf(contrato);
        assertThat(contrato).as("rotas do contrato não implementadas").isSubsetOf(implementado);
    }

    @Test
    void swaggerTemTituloEContatoDoContrato() throws Exception {
        JsonNode doc = apiDocs();
        assertThat(doc.at("/info/title").asText()).isEqualTo("Open Prontuário — MS Fila (Azure SQL)");
        assertThat(doc.at("/info/contact/name").asText()).contains("Luiz Fernando Grenteski");
        assertThat(doc.at("/components/securitySchemes/internalKey/name").asText()).isEqualTo("X-Internal-Key");
    }

    @SuppressWarnings("unchecked")
    private static Set<String> rotasDoContrato() throws Exception {
        try (Reader r = new FileReader("contract/ms-fila-sql.openapi.yaml")) {
            Map<String, Object> raiz = new Yaml().load(r);
            Map<String, Map<String, Object>> paths = (Map<String, Map<String, Object>>) raiz.get("paths");
            Set<String> rotas = new TreeSet<>();
            paths.forEach((caminho, item) -> item.keySet().stream().filter(METODOS::contains)
                    .forEach(m -> rotas.add(rota(m, caminho))));
            return rotas;
        }
    }

    private Set<String> rotasDoSpringdoc() throws Exception {
        Set<String> rotas = new TreeSet<>();
        JsonNode paths = apiDocs().get("paths");
        for (Iterator<String> it = paths.fieldNames(); it.hasNext(); ) {
            String caminho = it.next();
            paths.get(caminho).fieldNames().forEachRemaining(m -> {
                if (METODOS.contains(m)) rotas.add(rota(m, caminho));
            });
        }
        rotas.removeIf(r -> r.contains(" /actuator") || r.contains(" /v3/api-docs") || r.contains(" /swagger-ui"));
        return rotas;
    }

    private JsonNode apiDocs() throws Exception {
        String json = mvc.perform(get("/v3/api-docs")).andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return new ObjectMapper().readTree(json);
    }

    private static String rota(String metodo, String caminho) {
        return metodo.toUpperCase(Locale.ROOT) + " " + caminho.replaceAll("\\{[^}]+}", "{}");
    }
}
