package br.com.openprontuario.fila.api.fila;

import br.com.openprontuario.fila.api.comum.CabecalhoTexto;
import br.com.openprontuario.fila.application.fila.alterarprioridade.AlterarPrioridadeUseCase;
import br.com.openprontuario.fila.application.fila.avisarchegada.AvisarChegadaCommand;
import br.com.openprontuario.fila.application.fila.avisarchegada.AvisarChegadaUseCase;
import br.com.openprontuario.fila.application.fila.confirmarchegada.ConfirmarChegadaUseCase;
import br.com.openprontuario.fila.application.fila.iniciaratendimento.IniciarAtendimentoUseCase;
import br.com.openprontuario.fila.application.fila.listarfila.ListarFilaPorPrioridadeUseCase;
import br.com.openprontuario.fila.application.fila.removerdafila.RemoverDaFilaUseCase;
import br.com.openprontuario.fila.domain.model.MotivoSaida;
import br.com.openprontuario.fila.domain.model.StatusFila;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Fila da unidade. Atenção ao path {@code /fila/{id}}: no GET o id é a <b>unidade</b>, no DELETE
 * é o <b>registro</b> (mesmo path item no contrato, ver CONVENCOES §11).
 */
@RestController
@RequestMapping("/fila")
@Tag(name = "Fila")
public class FilaController {

    private final ListarFilaPorPrioridadeUseCase listar;
    private final AvisarChegadaUseCase avisar;
    private final ConfirmarChegadaUseCase confirmar;
    private final AlterarPrioridadeUseCase alterarPrioridade;
    private final IniciarAtendimentoUseCase iniciar;
    private final RemoverDaFilaUseCase remover;

    public FilaController(ListarFilaPorPrioridadeUseCase listar, AvisarChegadaUseCase avisar,
                          ConfirmarChegadaUseCase confirmar, AlterarPrioridadeUseCase alterarPrioridade,
                          IniciarAtendimentoUseCase iniciar, RemoverDaFilaUseCase remover) {
        this.listar = listar;
        this.avisar = avisar;
        this.confirmar = confirmar;
        this.alterarPrioridade = alterarPrioridade;
        this.iniciar = iniciar;
        this.remover = remover;
    }

    @GetMapping("/{id}")
    @Operation(operationId = "listarFilaPorPrioridade",
            summary = "RF08/RF09 — Fila ativa da unidade ordenada por prioridade (usuário lotado)")
    public List<RegistroFilaResponse> listar(
            @Parameter(description = "ID da **unidade** (unidadeId)") @PathVariable("id") String unidadeId,
            @RequestHeader("X-User-Id") String usuarioId,
            @RequestParam(required = false) StatusFila status) {
        return listar.executar(unidadeId, usuarioId, status).stream().map(RegistroFilaResponse::de).toList();
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(operationId = "removerDaFila",
            summary = "RF14 — Encerra o registro (→ FINALIZADO, sem apagar); publica PACIENTE_REMOVIDO")
    public void remover(
            @Parameter(description = "ID do **registro** de fila (registroId)") @PathVariable("id") String registroId,
            @RequestHeader("X-User-Id") String usuarioId,
            @RequestParam MotivoSaida motivo) {
        remover.executar(registroId, motivo, usuarioId);
    }

    @PostMapping("/{unidadeId}/aviso-chegada")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(operationId = "avisarChegada",
            summary = "RF03/RF04 — Paciente (X-User-Id) avisa que está a caminho; publica PACIENTE_A_CAMINHO")
    public RegistroFilaResponse avisarChegada(@PathVariable String unidadeId,
                                              @RequestHeader("X-User-Id") String usuarioId,
                                              @RequestHeader(value = "X-User-Name", required = false) String nome,
                                              @Valid @RequestBody AvisoChegadaRequest request) {
        return RegistroFilaResponse.de(avisar.executar(new AvisarChegadaCommand(usuarioId,
                CabecalhoTexto.decodificar(nome), unidadeId,
                request.sintomas(), request.tempoEstimadoChegadaMin())));
    }

    @PostMapping("/{registroId}/confirmar-chegada")
    @Operation(operationId = "confirmarChegada", summary = "RF10 — A_CAMINHO → AGUARDANDO; publica PACIENTE_CHEGOU")
    public RegistroFilaResponse confirmarChegada(@PathVariable String registroId,
                                                 @RequestHeader("X-User-Id") String usuarioId) {
        return RegistroFilaResponse.de(confirmar.executar(registroId, usuarioId));
    }

    @PatchMapping("/{registroId}/prioridade")
    @Operation(operationId = "alterarPrioridade",
            summary = "RF11 — Altera a prioridade (A_CAMINHO/AGUARDANDO); publica PRIORIDADE_ALTERADA")
    public RegistroFilaResponse alterarPrioridade(@PathVariable String registroId,
                                                  @RequestHeader("X-User-Id") String usuarioId,
                                                  @Valid @RequestBody AlterarPrioridadeRequest request) {
        return RegistroFilaResponse.de(alterarPrioridade.executar(registroId, request.prioridade(), usuarioId));
    }

    @PostMapping("/{registroId}/iniciar-atendimento")
    @Operation(operationId = "iniciarAtendimento",
            summary = "RF13 — AGUARDANDO → EM_ATENDIMENTO; publica ATENDIMENTO_INICIADO")
    public RegistroFilaResponse iniciarAtendimento(@PathVariable String registroId,
                                                   @RequestHeader("X-User-Id") String usuarioId) {
        return RegistroFilaResponse.de(iniciar.executar(registroId, usuarioId));
    }
}
