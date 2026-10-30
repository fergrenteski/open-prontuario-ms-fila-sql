package br.com.openprontuario.fila.api.comum;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.parameters.HeaderParameter;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Metadados do Swagger iguais aos do contrato {@code contract/ms-fila-sql.openapi.yaml}. */
@Configuration
public class OpenApiConfig {

    @Bean
    OpenAPI openApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Open Prontuário — MS Fila (Azure SQL)")
                        .version("1.0.0")
                        .description("""
                                Microserviço de **Unidades, Lotações e Fila**. Persistência em Azure SQL.
                                Publica os eventos de mudança de estado da fila no tópico `eventos-atendimento`
                                do Azure Service Bus (ver `asyncapi.yaml`).

                                Chamado somente pelo BFF (rotas de usuário) e pela Function / MS Prontuário
                                (rotas `/internal/**`). Todas as rotas exigem `X-Internal-Key`; as rotas de
                                usuário exigem também `X-User-Id` e `X-User-Roles` (ver `CONVENCOES.md`).
                                """)
                        .license(new License().name("Uso acadêmico - PUCPR"))
                        .contact(new Contact()
                                .name("Ana Paula Alves, Artur Bertoni, Bernardo Plottegher, Luiz Fernando Grenteski")))
                .components(new Components().addSecuritySchemes("internalKey", new SecurityScheme()
                        .type(SecurityScheme.Type.APIKEY).in(SecurityScheme.In.HEADER)
                        .name(ChaveInternaFilter.CABECALHO_CHAVE)))
                .addSecurityItem(new SecurityRequirement().addList("internalKey"));
    }

    /** Documenta X-User-Roles nas rotas de usuário (repassado pelo BFF; a autorização por perfil é do BFF). */
    @Bean
    OperationCustomizer cabecalhoDePerfis() {
        return (operation, handler) -> {
            boolean rotaDeUsuario = operation.getParameters() != null && operation.getParameters().stream()
                    .anyMatch(p -> ChaveInternaFilter.CABECALHO_USUARIO.equals(p.getName()));
            if (rotaDeUsuario) {
                operation.addParametersItem(new HeaderParameter().name("X-User-Roles").required(true)
                        .schema(new StringSchema().example("ATENDENTE")));
            }
            return operation;
        };
    }
}
