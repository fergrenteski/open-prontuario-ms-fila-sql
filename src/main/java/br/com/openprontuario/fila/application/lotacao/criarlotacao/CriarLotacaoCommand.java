package br.com.openprontuario.fila.application.lotacao.criarlotacao;

import br.com.openprontuario.fila.domain.model.PerfilUsuario;

public record CriarLotacaoCommand(String usuarioId, String unidadeId, PerfilUsuario perfil) {
}
