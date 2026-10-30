package br.com.openprontuario.fila.domain.policy;

import br.com.openprontuario.fila.domain.model.RegistroFila;

import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

/**
 * Ordena a fila: prioridade mais grave primeiro; desempate pelo horário de chegada
 * (ou de aviso, se ainda a caminho), mais antigo primeiro. FINALIZADO não aparece.
 */
public final class OrdenacaoFilaPolicy {

    private static final Comparator<RegistroFila> ORDEM = Comparator
            .comparing(RegistroFila::getPrioridade)
            .thenComparing(OrdenacaoFilaPolicy::horarioReferencia);

    private OrdenacaoFilaPolicy() {
    }

    public static List<RegistroFila> ordenar(Collection<RegistroFila> registros) {
        return registros.stream().filter(RegistroFila::isAtivo).sorted(ORDEM).toList();
    }

    private static OffsetDateTime horarioReferencia(RegistroFila r) {
        return r.getHorarioChegada() != null ? r.getHorarioChegada() : r.getHorarioAviso();
    }
}
