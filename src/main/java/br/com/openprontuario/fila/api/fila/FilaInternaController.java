package br.com.openprontuario.fila.api.fila;

import br.com.openprontuario.fila.application.fila.snapshot.ObterSnapshotFilaUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Rota interna da Function Visão Agregada (somente X-Internal-Key). */
@RestController
@Tag(name = "Interno")
public class FilaInternaController {

    private final ObterSnapshotFilaUseCase snapshot;

    public FilaInternaController(ObterSnapshotFilaUseCase snapshot) {
        this.snapshot = snapshot;
    }

    @GetMapping("/internal/fila/{unidadeId}/snapshot")
    @Operation(operationId = "snapshotFila", summary = "Estado para recálculo da visão agregada (usado pela Function)",
            description = "Registros com status ≠ FINALIZADO **ou** com `horarioInicioAtendimento` nas últimas 2 horas.")
    public List<RegistroFilaSnapshotResponse> snapshot(@PathVariable String unidadeId) {
        return snapshot.executar(unidadeId).stream().map(RegistroFilaSnapshotResponse::de).toList();
    }
}
