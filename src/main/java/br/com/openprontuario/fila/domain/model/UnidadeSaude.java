package br.com.openprontuario.fila.domain.model;

import br.com.openprontuario.fila.domain.exception.DadosInvalidosException;

import java.util.UUID;

/** Unidade de saúde (UBS ou UPA) com coordenadas geográficas. */
public record UnidadeSaude(String id, String nome, TipoUnidade tipo, double latitude, double longitude) {

    private static final double RAIO_TERRA_KM = 6371.0;

    public UnidadeSaude {
        if (id == null || id.isBlank()) throw new DadosInvalidosException("Unidade sem id");
        if (nome == null || nome.isBlank()) throw new DadosInvalidosException("Nome da unidade é obrigatório");
        if (tipo == null) throw new DadosInvalidosException("Tipo da unidade é obrigatório");
        if (latitude < -90 || latitude > 90) throw new DadosInvalidosException("Latitude fora de [-90, 90]");
        if (longitude < -180 || longitude > 180) throw new DadosInvalidosException("Longitude fora de [-180, 180]");
    }

    public static UnidadeSaude nova(String nome, TipoUnidade tipo, double latitude, double longitude) {
        return new UnidadeSaude(UUID.randomUUID().toString(), nome, tipo, latitude, longitude);
    }

    public UnidadeSaude atualizada(String nome, TipoUnidade tipo, double latitude, double longitude) {
        return new UnidadeSaude(id, nome, tipo, latitude, longitude);
    }

    /** Distância em km até o ponto informado (fórmula de Haversine). */
    public double calcularDistancia(double lat, double lon) {
        double dLat = Math.toRadians(lat - latitude);
        double dLon = Math.toRadians(lon - longitude);
        double a = Math.pow(Math.sin(dLat / 2), 2)
                + Math.cos(Math.toRadians(latitude)) * Math.cos(Math.toRadians(lat)) * Math.pow(Math.sin(dLon / 2), 2);
        return 2 * RAIO_TERRA_KM * Math.asin(Math.sqrt(a));
    }
}
