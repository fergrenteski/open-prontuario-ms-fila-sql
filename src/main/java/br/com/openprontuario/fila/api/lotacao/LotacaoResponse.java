package br.com.openprontuario.fila.api.lotacao;

import br.com.openprontuario.fila.domain.model.Lotacao;
import br.com.openprontuario.fila.domain.model.PerfilUsuario;

public record LotacaoResponse(String id, String usuarioId, String unidadeId, PerfilUsuario perfil) {

    static LotacaoResponse de(Lotacao l) {
        return new LotacaoResponse(l.id(), l.usuarioId(), l.unidadeId(), l.perfil());
    }
}
