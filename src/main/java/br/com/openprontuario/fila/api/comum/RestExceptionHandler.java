package br.com.openprontuario.fila.api.comum;

import br.com.openprontuario.fila.domain.exception.AcessoNegadoException;
import br.com.openprontuario.fila.domain.exception.DadosInvalidosException;
import br.com.openprontuario.fila.domain.exception.DomainException;
import br.com.openprontuario.fila.domain.exception.LotacaoDuplicadaException;
import br.com.openprontuario.fila.domain.exception.RecursoNaoEncontradoException;
import br.com.openprontuario.fila.domain.exception.RegistroAtivoDuplicadoException;
import br.com.openprontuario.fila.domain.exception.TransicaoInvalidaException;
import br.com.openprontuario.fila.domain.exception.UnidadeEmUsoException;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * Converte exceções em {@code application/problem+json} (RFC 9457) com {@code codigo} e
 * {@code correlationId}. Nunca expõe stack trace, SQL ou dado clínico.
 */
@RestControllerAdvice
public class RestExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger LOG = LoggerFactory.getLogger(RestExceptionHandler.class);

    private static final Map<Class<? extends DomainException>, CodigoErro> CODIGOS = Map.of(
            DadosInvalidosException.class, CodigoErro.REQUISICAO_INVALIDA,
            AcessoNegadoException.class, CodigoErro.SEM_PERMISSAO,
            RecursoNaoEncontradoException.class, CodigoErro.RECURSO_NAO_ENCONTRADO,
            TransicaoInvalidaException.class, CodigoErro.TRANSICAO_INVALIDA,
            RegistroAtivoDuplicadoException.class, CodigoErro.REGISTRO_ATIVO_DUPLICADO,
            LotacaoDuplicadaException.class, CodigoErro.LOTACAO_DUPLICADA,
            UnidadeEmUsoException.class, CodigoErro.UNIDADE_EM_USO);

    @ExceptionHandler(DomainException.class)
    ResponseEntity<ProblemDetail> negocio(DomainException ex, HttpServletRequest req) {
        CodigoErro codigo = CODIGOS.getOrDefault(ex.getClass(), CodigoErro.REQUISICAO_INVALIDA);
        return responder(codigo, ex.getMessage(), req);
    }

    @ExceptionHandler(DataAccessException.class)
    ResponseEntity<ProblemDetail> banco(DataAccessException ex, HttpServletRequest req) {
        LOG.error("Falha de acesso ao banco", ex);
        return responder(CodigoErro.DEPENDENCIA_INDISPONIVEL, "Banco de dados indisponível no momento", req);
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<ProblemDetail> inesperado(Exception ex, HttpServletRequest req) {
        LOG.error("Erro inesperado", ex);
        return responder(CodigoErro.ERRO_INTERNO, "Erro inesperado; informe o correlationId ao suporte", req);
    }

    /** Erros do próprio Spring MVC (validação, JSON malformado, parâmetro ausente, 404, 405...). */
    @Override
    protected ResponseEntity<Object> handleExceptionInternal(Exception ex, Object body, HttpHeaders headers,
                                                             HttpStatusCode status, WebRequest request) {
        ResponseEntity<Object> resposta = super.handleExceptionInternal(ex, body, headers, status, request);
        if (resposta != null && resposta.getBody() instanceof ProblemDetail p) {
            String instancia = request instanceof ServletWebRequest s ? s.getRequest().getRequestURI() : null;
            Problemas.completar(p, CodigoErro.porStatus(status.value()), instancia);
            String detalheValidacao = detalheDeValidacao(ex);
            if (detalheValidacao != null) {
                p.setDetail(detalheValidacao);
            }
        }
        return resposta;
    }

    private static String detalheDeValidacao(Exception ex) {
        if (ex instanceof MethodArgumentNotValidException e) {
            return e.getBindingResult().getFieldErrors().stream()
                    .map(f -> f.getField() + ": " + f.getDefaultMessage())
                    .sorted().collect(Collectors.joining("; "));
        }
        if (ex instanceof HandlerMethodValidationException e) {
            return e.getAllErrors().stream().map(m -> m.getDefaultMessage())
                    .sorted().collect(Collectors.joining("; "));
        }
        return null;
    }

    private static ResponseEntity<ProblemDetail> responder(CodigoErro codigo, String detalhe, HttpServletRequest req) {
        return ResponseEntity.status(codigo.status()).body(Problemas.criar(codigo, detalhe, req.getRequestURI()));
    }
}
