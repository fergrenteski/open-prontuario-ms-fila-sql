package br.com.openprontuario.fila.application.unidade.listarproximas;

import br.com.openprontuario.fila.application.unidade.ports.UnidadeSaudeRepository;

import java.util.Comparator;
import java.util.List;

/** RF02 — unidades dentro do raio, da mais próxima para a mais distante (Haversine no domínio). */
public class ListarUnidadesProximasUseCase {

    private final UnidadeSaudeRepository unidades;

    public ListarUnidadesProximasUseCase(UnidadeSaudeRepository unidades) {
        this.unidades = unidades;
    }

    // ponytail: filtra em memória; com milhares de unidades, filtrar antes por bounding box no SQL
    public List<UnidadeProximaResult> executar(double latitude, double longitude, int raioKm) {
        return unidades.listar(null).stream()
                .map(u -> new UnidadeProximaResult(u, u.calcularDistancia(latitude, longitude)))
                .filter(r -> r.distanciaKm() <= raioKm)
                .sorted(Comparator.comparingDouble(UnidadeProximaResult::distanciaKm))
                .toList();
    }
}
