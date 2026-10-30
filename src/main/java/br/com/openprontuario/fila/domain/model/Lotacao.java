package br.com.openprontuario.fila.domain.model;

import br.com.openprontuario.fila.domain.exception.DadosInvalidosException;

import java.util.UUID;

/** Vínculo de um profissional (usuarioId = oid) com uma unidade, com o perfil exercido nela. */
public record Lotacao(String id, String usuarioId, String unidadeId, PerfilUsuario perfil) {

    public Lotacao {
        if (usuarioId == null || usuarioId.isBlank()) throw new DadosInvalidosException("usuarioId é obrigatório");
        if (unidadeId == null || unidadeId.isBlank()) throw new DadosInvalidosException("unidadeId é obrigatório");
        if (perfil == null) throw new DadosInvalidosException("perfil é obrigatório");
    }

    public static Lotacao nova(String usuarioId, String unidadeId, PerfilUsuario perfil) {
        return new Lotacao(UUID.randomUUID().toString(), usuarioId, unidadeId, perfil);
    }
}
