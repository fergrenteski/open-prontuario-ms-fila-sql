package br.com.openprontuario.fila.application.unidade.listarproximas;

import br.com.openprontuario.fila.domain.model.UnidadeSaude;

public record UnidadeProximaResult(UnidadeSaude unidade, double distanciaKm) {
}
