package br.com.openprontuario.fila.infrastructure.persistence;

import br.com.openprontuario.fila.domain.model.TipoUnidade;
import br.com.openprontuario.fila.domain.model.UnidadeSaude;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "unidades")
class UnidadeEntity {

    @Id
    private String id;
    private String nome;
    @Enumerated(EnumType.STRING)
    private TipoUnidade tipo;
    private double latitude;
    private double longitude;

    protected UnidadeEntity() {
    }

    static UnidadeEntity de(UnidadeSaude u) {
        UnidadeEntity e = new UnidadeEntity();
        e.id = u.id();
        e.nome = u.nome();
        e.tipo = u.tipo();
        e.latitude = u.latitude();
        e.longitude = u.longitude();
        return e;
    }

    UnidadeSaude paraDominio() {
        return new UnidadeSaude(id, nome, tipo, latitude, longitude);
    }
}
