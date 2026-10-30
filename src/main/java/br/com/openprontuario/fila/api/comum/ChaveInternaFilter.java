package br.com.openprontuario.fila.api.comum;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/**
 * Só o BFF (e serviços internos) chamam este serviço: toda rota exige {@code X-Internal-Key}
 * (comparação em tempo constante), exceto health e Swagger. Rotas de usuário (fora de
 * {@code /internal/**}) exigem também {@code X-User-Id}. Falha → 401 NAO_AUTENTICADO.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
public class ChaveInternaFilter extends OncePerRequestFilter {

    public static final String CABECALHO_CHAVE = "X-Internal-Key";
    public static final String CABECALHO_USUARIO = "X-User-Id";

    private final byte[] chave;
    private final ObjectMapper json;

    public ChaveInternaFilter(@Value("${openprontuario.internal-api-key}") String chave, ObjectMapper json) {
        if (chave == null || chave.isBlank()) {
            throw new IllegalStateException("INTERNAL_API_KEY não configurada");
        }
        this.chave = chave.getBytes(StandardCharsets.UTF_8);
        this.json = json;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String caminho = caminho(request);
        return caminho.equals("/actuator/health") || caminho.startsWith("/actuator/health/")
                || caminho.startsWith("/v3/api-docs") || caminho.startsWith("/swagger-ui");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String recebida = request.getHeader(CABECALHO_CHAVE);
        if (recebida == null || !MessageDigest.isEqual(chave, recebida.getBytes(StandardCharsets.UTF_8))) {
            negar(request, response, "Cabeçalho X-Internal-Key ausente ou inválido");
            return;
        }
        String usuario = request.getHeader(CABECALHO_USUARIO);
        if (!caminho(request).startsWith("/internal/") && (usuario == null || usuario.isBlank())) {
            negar(request, response, "Cabeçalho X-User-Id ausente");
            return;
        }
        chain.doFilter(request, response);
    }

    private void negar(HttpServletRequest request, HttpServletResponse response, String detalhe) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        json.writeValue(response.getOutputStream(),
                Problemas.criar(CodigoErro.NAO_AUTENTICADO, detalhe, request.getRequestURI()));
    }

    private static String caminho(HttpServletRequest request) {
        return request.getRequestURI().substring(request.getContextPath().length());
    }
}
