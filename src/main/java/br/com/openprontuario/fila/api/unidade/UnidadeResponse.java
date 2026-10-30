package br.com.openprontuario.fila.api.unidade;

import br.com.openprontuario.fila.domain.model.TipoUnidade;
import br.com.openprontuario.fila.domain.model.UnidadeSaude;

public record UnidadeResponse(String id, String nome, TipoUnidade tipo, double latitude, double longitude) {

    static UnidadeResponse de(UnidadeSaude u) {
        return new UnidadeResponse(u.id(), u.nome(), u.tipo(), u.latitude(), u.longitude());
    }
}
