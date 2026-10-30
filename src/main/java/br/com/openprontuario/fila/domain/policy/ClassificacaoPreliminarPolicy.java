package br.com.openprontuario.fila.domain.policy;

import br.com.openprontuario.fila.domain.model.Prioridade;

import java.text.Normalizer;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Sugere a prioridade preliminar a partir dos sintomas informados no aviso de chegada.
 * Comparação sem acento e sem caixa, por "contém"; vale o nível mais grave encontrado.
 * A classificação definitiva é feita pelo profissional na triagem.
 */
public final class ClassificacaoPreliminarPolicy {

    private static final Map<Prioridade, List<String>> TERMOS = Map.of(
            Prioridade.EMERGENCIA, List.of("parada", "inconsciente", "convulsao", "hemorragia"),
            Prioridade.MUITO_URGENTE, List.of("dor no peito", "falta de ar", "desmaio", "avc"),
            Prioridade.URGENTE, List.of("febre alta", "vomito", "fratura", "dor intensa"),
            Prioridade.POUCO_URGENTE, List.of("febre", "dor", "tontura"));

    private ClassificacaoPreliminarPolicy() {
    }

    public static Prioridade classificar(Collection<String> sintomas) {
        Prioridade resultado = Prioridade.NAO_URGENTE;
        for (String sintoma : sintomas) {
            String texto = normalizar(sintoma);
            for (Prioridade nivel : Prioridade.values()) {
                if (nivel.ordinal() >= resultado.ordinal()) break;
                if (TERMOS.getOrDefault(nivel, List.of()).stream().anyMatch(texto::contains)) {
                    resultado = nivel;
                    break;
                }
            }
        }
        return resultado;
    }

    private static String normalizar(String texto) {
        return Normalizer.normalize(texto, Normalizer.Form.NFD).replaceAll("\\p{M}", "").toLowerCase(Locale.ROOT);
    }
}
