package br.com.openprontuario.fila.api.comum;

import org.springframework.http.HttpStatus;

import java.util.Locale;

/** Códigos de erro compartilhados (CONVENCOES §4), devolvidos no campo {@code codigo} do Problem Details. */
public enum CodigoErro {
    REQUISICAO_INVALIDA(HttpStatus.BAD_REQUEST, "Requisição inválida"),
    NAO_AUTENTICADO(HttpStatus.UNAUTHORIZED, "Não autenticado"),
    SEM_PERMISSAO(HttpStatus.FORBIDDEN, "Sem permissão"),
    RECURSO_NAO_ENCONTRADO(HttpStatus.NOT_FOUND, "Recurso não encontrado"),
    TRANSICAO_INVALIDA(HttpStatus.CONFLICT, "Transição de estado inválida"),
    REGISTRO_ATIVO_DUPLICADO(HttpStatus.CONFLICT, "Registro ativo duplicado"),
    LOTACAO_DUPLICADA(HttpStatus.CONFLICT, "Lotação duplicada"),
    UNIDADE_EM_USO(HttpStatus.CONFLICT, "Unidade em uso"),
    DEPENDENCIA_INDISPONIVEL(HttpStatus.SERVICE_UNAVAILABLE, "Dependência indisponível"),
    ERRO_INTERNO(HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno");

    private final HttpStatus status;
    private final String titulo;

    CodigoErro(HttpStatus status, String titulo) {
        this.status = status;
        this.titulo = titulo;
    }

    public HttpStatus status() {
        return status;
    }

    public String titulo() {
        return titulo;
    }

    public String tipo() {
        return "https://openprontuario.dev/erros/" + name().toLowerCase(Locale.ROOT).replace('_', '-');
    }

    /** Código padrão para erros gerados pelo próprio Spring MVC (400, 404, 405...). */
    public static CodigoErro porStatus(int status) {
        return switch (status) {
            case 401 -> NAO_AUTENTICADO;
            case 403 -> SEM_PERMISSAO;
            case 404 -> RECURSO_NAO_ENCONTRADO;
            case 503 -> DEPENDENCIA_INDISPONIVEL;
            default -> status >= 500 ? ERRO_INTERNO : REQUISICAO_INVALIDA;
        };
    }
}
