package br.com.openprontuario.fila.api.comum;

import org.slf4j.MDC;
import org.springframework.http.ProblemDetail;

import java.net.URI;

/** Monta respostas RFC 9457 com {@code codigo} e {@code correlationId}. */
public final class Problemas {

    private Problemas() {
    }

    public static ProblemDetail criar(CodigoErro codigo, String detalhe, String instancia) {
        ProblemDetail p = ProblemDetail.forStatusAndDetail(codigo.status(), detalhe);
        completar(p, codigo, instancia);
        return p;
    }

    static void completar(ProblemDetail p, CodigoErro codigo, String instancia) {
        p.setType(URI.create(codigo.tipo()));
        p.setTitle(codigo.titulo());
        if (instancia != null) {
            p.setInstance(URI.create(instancia));
        }
        p.setProperty("codigo", codigo.name());
        p.setProperty("correlationId", MDC.get(CorrelationIdFilter.MDC_CHAVE));
    }
}
