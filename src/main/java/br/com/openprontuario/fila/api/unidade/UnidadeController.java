package br.com.openprontuario.fila.api.unidade;

import br.com.openprontuario.fila.application.unidade.atualizarunidade.AtualizarUnidadeUseCase;
import br.com.openprontuario.fila.application.unidade.buscarunidade.BuscarUnidadeUseCase;
import br.com.openprontuario.fila.application.unidade.criarunidade.CriarUnidadeUseCase;
import br.com.openprontuario.fila.application.unidade.listarminhas.ListarMinhasUnidadesUseCase;
import br.com.openprontuario.fila.application.unidade.listarproximas.ListarUnidadesProximasUseCase;
import br.com.openprontuario.fila.application.unidade.listarunidades.ListarUnidadesUseCase;
import br.com.openprontuario.fila.application.unidade.removerunidade.RemoverUnidadeUseCase;
import br.com.openprontuario.fila.domain.model.TipoUnidade;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** CRUD de unidades (perfil GESTOR verificado no BFF), unidades próximas (RF02) e minhas unidades (RF07). */
@RestController
@RequestMapping("/unidades")
@Tag(name = "Unidades")
public class UnidadeController {

    private final ListarUnidadesUseCase listar;
    private final ListarUnidadesProximasUseCase proximas;
    private final ListarMinhasUnidadesUseCase minhas;
    private final BuscarUnidadeUseCase buscar;
    private final CriarUnidadeUseCase criar;
    private final AtualizarUnidadeUseCase atualizar;
    private final RemoverUnidadeUseCase remover;

    public UnidadeController(ListarUnidadesUseCase listar, ListarUnidadesProximasUseCase proximas,
                             ListarMinhasUnidadesUseCase minhas, BuscarUnidadeUseCase buscar,
                             CriarUnidadeUseCase criar, AtualizarUnidadeUseCase atualizar,
                             RemoverUnidadeUseCase remover) {
        this.listar = listar;
        this.proximas = proximas;
        this.minhas = minhas;
        this.buscar = buscar;
        this.criar = criar;
        this.atualizar = atualizar;
        this.remover = remover;
    }

    @GetMapping
    @Operation(operationId = "listarUnidades", summary = "Lista todas as unidades de saúde")
    public List<UnidadeResponse> listar(@RequestHeader("X-User-Id") String usuarioId,
                                        @RequestParam(required = false) TipoUnidade tipo) {
        return listar.executar(tipo).stream().map(UnidadeResponse::de).toList();
    }

    @GetMapping("/proximas")
    @Operation(operationId = "listarUnidadesProximas",
            summary = "RF02 — Unidades dentro do raio, ordenadas por distância (Haversine)")
    public List<UnidadeComDistanciaResponse> proximas(
            @RequestHeader("X-User-Id") String usuarioId,
            @RequestParam @DecimalMin("-90") @DecimalMax("90") double latitude,
            @RequestParam @DecimalMin("-180") @DecimalMax("180") double longitude,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int raioKm) {
        return proximas.executar(latitude, longitude, raioKm).stream().map(UnidadeComDistanciaResponse::de).toList();
    }

    @GetMapping("/minhas")
    @Operation(operationId = "listarMinhasUnidades",
            summary = "RF07 — Unidades em que o usuário (X-User-Id) está lotado")
    public List<UnidadeResponse> minhas(@RequestHeader("X-User-Id") String usuarioId) {
        return minhas.executar(usuarioId).stream().map(UnidadeResponse::de).toList();
    }

    @GetMapping("/{unidadeId}")
    @Operation(operationId = "buscarUnidade", summary = "Detalha uma unidade")
    public UnidadeResponse buscar(@PathVariable String unidadeId, @RequestHeader("X-User-Id") String usuarioId) {
        return UnidadeResponse.de(buscar.executar(unidadeId));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(operationId = "criarUnidade", summary = "Cadastra uma unidade de saúde (GESTOR)")
    public UnidadeResponse criar(@RequestHeader("X-User-Id") String usuarioId,
                                 @Valid @RequestBody UnidadeRequest request) {
        return UnidadeResponse.de(criar.executar(request.paraCommand()));
    }

    @PutMapping("/{unidadeId}")
    @Operation(operationId = "atualizarUnidade", summary = "Atualiza uma unidade (GESTOR)")
    public UnidadeResponse atualizar(@PathVariable String unidadeId, @RequestHeader("X-User-Id") String usuarioId,
                                     @Valid @RequestBody UnidadeRequest request) {
        return UnidadeResponse.de(atualizar.executar(unidadeId, request.paraCommand()));
    }

    @DeleteMapping("/{unidadeId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(operationId = "removerUnidade",
            summary = "Remove uma unidade sem registros de fila nem lotações (GESTOR)")
    public void remover(@PathVariable String unidadeId, @RequestHeader("X-User-Id") String usuarioId) {
        remover.executar(unidadeId);
    }
}
