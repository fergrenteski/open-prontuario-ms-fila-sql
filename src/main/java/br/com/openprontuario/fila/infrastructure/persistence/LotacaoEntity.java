package br.com.openprontuario.fila.infrastructure.persistence;

import br.com.openprontuario.fila.domain.model.Lotacao;
import br.com.openprontuario.fila.domain.model.PerfilUsuario;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "lotacoes")
class LotacaoEntity {

    @Id
    private String id;
    @Column(name = "usuario_id")
    private String usuarioId;
    @Column(name = "unidade_id")
    private String unidadeId;
    @Enumerated(EnumType.STRING)
    private PerfilUsuario perfil;

    protected LotacaoEntity() {
    }

    static LotacaoEntity de(Lotacao l) {
        LotacaoEntity e = new LotacaoEntity();
        e.id = l.id();
        e.usuarioId = l.usuarioId();
        e.unidadeId = l.unidadeId();
        e.perfil = l.perfil();
        return e;
    }

    Lotacao paraDominio() {
        return new Lotacao(id, usuarioId, unidadeId, perfil);
    }
}
