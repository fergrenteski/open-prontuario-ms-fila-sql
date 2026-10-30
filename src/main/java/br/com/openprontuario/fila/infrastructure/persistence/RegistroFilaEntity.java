package br.com.openprontuario.fila.infrastructure.persistence;

import br.com.openprontuario.fila.domain.model.MotivoSaida;
import br.com.openprontuario.fila.domain.model.Prioridade;
import br.com.openprontuario.fila.domain.model.RegistroFila;
import br.com.openprontuario.fila.domain.model.StatusFila;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;
import java.util.List;

@Entity
@Table(name = "registros_fila")
class RegistroFilaEntity {

    @Id
    private String id;
    @Column(name = "paciente_id")
    private String pacienteId;
    @Column(name = "unidade_id")
    private String unidadeId;
    @Enumerated(EnumType.STRING)
    private Prioridade prioridade;
    @Enumerated(EnumType.STRING)
    private StatusFila status;
    @Convert(converter = SintomasJsonConverter.class)
    private List<String> sintomas;
    @Column(name = "tempo_estimado_chegada_min")
    private Integer tempoEstimadoChegadaMin;
    @Column(name = "horario_aviso")
    private OffsetDateTime horarioAviso;
    @Column(name = "horario_chegada")
    private OffsetDateTime horarioChegada;
    @Column(name = "horario_inicio_atendimento")
    private OffsetDateTime horarioInicioAtendimento;
    @Enumerated(EnumType.STRING)
    @Column(name = "motivo_saida")
    private MotivoSaida motivoSaida;

    protected RegistroFilaEntity() {
    }

    static RegistroFilaEntity de(RegistroFila r) {
        RegistroFilaEntity e = new RegistroFilaEntity();
        e.id = r.getId();
        e.pacienteId = r.getPacienteId();
        e.unidadeId = r.getUnidadeId();
        e.prioridade = r.getPrioridade();
        e.status = r.getStatus();
        e.sintomas = r.getSintomas();
        e.tempoEstimadoChegadaMin = r.getTempoEstimadoChegadaMin();
        e.horarioAviso = r.getHorarioAviso();
        e.horarioChegada = r.getHorarioChegada();
        e.horarioInicioAtendimento = r.getHorarioInicioAtendimento();
        e.motivoSaida = r.getMotivoSaida();
        return e;
    }

    RegistroFila paraDominio() {
        return RegistroFila.restaurar(id, pacienteId, unidadeId, prioridade, status, sintomas, tempoEstimadoChegadaMin,
                horarioAviso, horarioChegada, horarioInicioAtendimento, motivoSaida);
    }
}
