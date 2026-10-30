package br.com.openprontuario.fila.api.unidade;

import br.com.openprontuario.fila.application.unidade.listarproximas.UnidadeProximaResult;
import br.com.openprontuario.fila.domain.model.TipoUnidade;

public record UnidadeComDistanciaResponse(String id, String nome, TipoUnidade tipo, double latitude, double longitude,
                                          double distanciaKm) {

    static UnidadeComDistanciaResponse de(UnidadeProximaResult r) {
        var u = r.unidade();
        return new UnidadeComDistanciaResponse(u.id(), u.nome(), u.tipo(), u.latitude(), u.longitude(),
                Math.round(r.distanciaKm() * 100) / 100.0);
    }
}
