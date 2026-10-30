package br.com.openprontuario.fila.api.lotacao;

import br.com.openprontuario.fila.application.lotacao.criarlotacao.CriarLotacaoUseCase;
import br.com.openprontuario.fila.application.lotacao.listarlotacoes.ListarLotacoesUseCase;
import br.com.openprontuario.fila.application.lotacao.removerlotacao.RemoverLotacaoUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Vínculos profissional × unidade (perfil GESTOR verificado no BFF). */
@RestController
@RequestMapping("/lotacoes")
@Tag(name = "Lotacoes")
public class LotacaoController {

    private final ListarLotacoesUseCase listar;
    private final CriarLotacaoUseCase criar;
    private final RemoverLotacaoUseCase remover;

    public LotacaoController(ListarLotacoesUseCase listar, CriarLotacaoUseCase criar, RemoverLotacaoUseCase remover) {
        this.listar = listar;
        this.criar = criar;
        this.remover = remover;
    }

    @GetMapping
    @Operation(operationId = "listarLotacoes", summary = "Lista lotações de uma unidade (GESTOR)")
    public List<LotacaoResponse> listar(@RequestHeader("X-User-Id") String usuarioId,
                                        @RequestParam String unidadeId) {
        return listar.executar(unidadeId).stream().map(LotacaoResponse::de).toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(operationId = "criarLotacao", summary = "Vincula um profissional a uma unidade (GESTOR)")
    public LotacaoResponse criar(@RequestHeader("X-User-Id") String usuarioId,
                                 @Valid @RequestBody LotacaoRequest request) {
        return LotacaoResponse.de(criar.executar(request.paraCommand()));
    }

    @DeleteMapping("/{lotacaoId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(operationId = "removerLotacao", summary = "Remove o vínculo (GESTOR)")
    public void remover(@PathVariable String lotacaoId, @RequestHeader("X-User-Id") String usuarioId) {
        remover.executar(lotacaoId);
    }
}
