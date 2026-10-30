package br.com.openprontuario.fila.application.fila.snapshot;

import br.com.openprontuario.fila.application.fila.ports.RegistroFilaRepository;
import br.com.openprontuario.fila.domain.model.RegistroFila;

import java.time.Clock;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.List;

/**
 * Estado para o recálculo da visão agregada (Function): registros ativos ou com início de
 * atendimento nas últimas 2 horas.
 */
public class ObterSnapshotFilaUseCase {

    static final Duration JANELA_ATENDIMENTOS = Duration.ofHours(2);

    private final RegistroFilaRepository registros;
    private final Clock relogio;

    public ObterSnapshotFilaUseCase(RegistroFilaRepository registros, Clock relogio) {
        this.registros = registros;
        this.relogio = relogio;
    }

    public List<RegistroFila> executar(String unidadeId) {
        return registros.listarParaSnapshot(unidadeId, OffsetDateTime.now(relogio).minus(JANELA_ATENDIMENTOS));
    }
}
