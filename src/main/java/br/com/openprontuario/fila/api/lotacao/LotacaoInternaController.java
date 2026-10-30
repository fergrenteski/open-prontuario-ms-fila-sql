package br.com.openprontuario.fila.api.lotacao;

import br.com.openprontuario.fila.application.lotacao.listarlotacoesdousuario.ListarLotacoesDoUsuarioUseCase;
import br.com.openprontuario.fila.application.lotacao.verificarlotacao.VerificarLotacaoUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Rotas internas usadas pelo MS Prontuário (somente X-Internal-Key). */
@RestController
@RequestMapping("/internal/lotacoes")
@Tag(name = "Interno")
public class LotacaoInternaController {

    private final ListarLotacoesDoUsuarioUseCase listarDoUsuario;
    private final VerificarLotacaoUseCase verificar;

    public LotacaoInternaController(ListarLotacoesDoUsuarioUseCase listarDoUsuario, VerificarLotacaoUseCase verificar) {
        this.listarDoUsuario = listarDoUsuario;
        this.verificar = verificar;
    }

    public record VerificacaoLotacaoResponse(boolean lotado) {
    }

    @GetMapping
    @Operation(operationId = "listarLotacoesDoUsuario", summary = "Lotações de um usuário (usado pelo MS Prontuário)")
    public List<LotacaoResponse> listarDoUsuario(@RequestParam String usuarioId) {
        return listarDoUsuario.executar(usuarioId).stream().map(LotacaoResponse::de).toList();
    }

    @GetMapping("/verificar")
    @Operation(operationId = "verificarLotacao",
            summary = "Verifica se o usuário está lotado na unidade (usado pelo MS Prontuário)")
    public VerificacaoLotacaoResponse verificar(@RequestParam String usuarioId, @RequestParam String unidadeId) {
        return new VerificacaoLotacaoResponse(verificar.executar(usuarioId, unidadeId));
    }
}
