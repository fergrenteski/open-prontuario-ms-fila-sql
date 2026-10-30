package br.com.openprontuario.fila.api.unidade;

import br.com.openprontuario.fila.application.unidade.DadosUnidadeCommand;
import br.com.openprontuario.fila.domain.model.TipoUnidade;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UnidadeRequest(
        @NotBlank @Size(min = 3, max = 120) String nome,
        @NotNull TipoUnidade tipo,
        @NotNull @DecimalMin("-90") @DecimalMax("90") Double latitude,
        @NotNull @DecimalMin("-180") @DecimalMax("180") Double longitude) {

    DadosUnidadeCommand paraCommand() {
        return new DadosUnidadeCommand(nome, tipo, latitude, longitude);
    }
}
