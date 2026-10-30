package br.com.openprontuario.fila.api.lotacao;

import br.com.openprontuario.fila.application.lotacao.criarlotacao.CriarLotacaoCommand;
import br.com.openprontuario.fila.domain.model.PerfilUsuario;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record LotacaoRequest(@NotBlank @Size(max = 64) String usuarioId,
                             @NotBlank @Size(max = 64) String unidadeId,
                             @NotNull PerfilUsuario perfil) {

    CriarLotacaoCommand paraCommand() {
        return new CriarLotacaoCommand(usuarioId, unidadeId, perfil);
    }
}
