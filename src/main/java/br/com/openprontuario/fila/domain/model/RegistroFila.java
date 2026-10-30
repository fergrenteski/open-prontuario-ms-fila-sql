package br.com.openprontuario.fila.domain.model;

import br.com.openprontuario.fila.domain.exception.DadosInvalidosException;
import br.com.openprontuario.fila.domain.exception.TransicaoInvalidaException;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Registro de um paciente na fila de uma unidade. As transições da máquina de estados
 * (arc42 §6.11) são métodos desta entidade; qualquer transição não prevista lança
 * {@link TransicaoInvalidaException}.
 */
public class RegistroFila {

    private final String id;
    private final String pacienteId;
    private final String unidadeId;
    private Prioridade prioridade;
    private StatusFila status;
    private final List<String> sintomas;
    private final Integer tempoEstimadoChegadaMin;
    private final OffsetDateTime horarioAviso;
    private OffsetDateTime horarioChegada;
    private OffsetDateTime horarioInicioAtendimento;
    private MotivoSaida motivoSaida;

    private RegistroFila(String id, String pacienteId, String unidadeId, Prioridade prioridade, StatusFila status,
                         List<String> sintomas, Integer tempoEstimadoChegadaMin, OffsetDateTime horarioAviso,
                         OffsetDateTime horarioChegada, OffsetDateTime horarioInicioAtendimento, MotivoSaida motivoSaida) {
        this.id = Objects.requireNonNull(id);
        this.pacienteId = Objects.requireNonNull(pacienteId);
        this.unidadeId = Objects.requireNonNull(unidadeId);
        this.prioridade = Objects.requireNonNull(prioridade);
        this.status = Objects.requireNonNull(status);
        this.sintomas = List.copyOf(sintomas);
        this.tempoEstimadoChegadaMin = tempoEstimadoChegadaMin;
        this.horarioAviso = Objects.requireNonNull(horarioAviso);
        this.horarioChegada = horarioChegada;
        this.horarioInicioAtendimento = horarioInicioAtendimento;
        this.motivoSaida = motivoSaida;
    }

    /** Paciente avisa que está a caminho: cria o registro em {@code A_CAMINHO}. */
    public static RegistroFila avisar(String pacienteId, String unidadeId, List<String> sintomas,
                                      Integer tempoEstimadoChegadaMin, Prioridade prioridadePreliminar,
                                      OffsetDateTime agora) {
        if (pacienteId == null || pacienteId.isBlank()) throw new DadosInvalidosException("pacienteId é obrigatório");
        if (sintomas == null || sintomas.isEmpty()) throw new DadosInvalidosException("Informe ao menos um sintoma");
        return new RegistroFila(UUID.randomUUID().toString(), pacienteId, unidadeId, prioridadePreliminar,
                StatusFila.A_CAMINHO, sintomas, tempoEstimadoChegadaMin, agora, null, null, null);
    }

    /** Reconstrói um registro já persistido (usado pelos adapters de persistência). */
    public static RegistroFila restaurar(String id, String pacienteId, String unidadeId, Prioridade prioridade,
                                         StatusFila status, List<String> sintomas, Integer tempoEstimadoChegadaMin,
                                         OffsetDateTime horarioAviso, OffsetDateTime horarioChegada,
                                         OffsetDateTime horarioInicioAtendimento, MotivoSaida motivoSaida) {
        return new RegistroFila(id, pacienteId, unidadeId, prioridade, status, sintomas, tempoEstimadoChegadaMin,
                horarioAviso, horarioChegada, horarioInicioAtendimento, motivoSaida);
    }

    /** {@code A_CAMINHO → AGUARDANDO}. */
    public void confirmarChegada(OffsetDateTime agora) {
        exigirStatus(StatusFila.A_CAMINHO, StatusFila.AGUARDANDO);
        this.status = StatusFila.AGUARDANDO;
        this.horarioChegada = agora;
    }

    /** Permitido apenas antes do atendimento ({@code A_CAMINHO} ou {@code AGUARDANDO}). */
    public void alterarPrioridade(Prioridade nova) {
        if (status != StatusFila.A_CAMINHO && status != StatusFila.AGUARDANDO) {
            throw new TransicaoInvalidaException("Prioridade só pode mudar antes do atendimento (status " + status + ")");
        }
        this.prioridade = Objects.requireNonNull(nova);
    }

    /** {@code AGUARDANDO → EM_ATENDIMENTO}. */
    public void iniciarAtendimento(OffsetDateTime agora) {
        exigirStatus(StatusFila.AGUARDANDO, StatusFila.EM_ATENDIMENTO);
        this.status = StatusFila.EM_ATENDIMENTO;
        this.horarioInicioAtendimento = agora;
    }

    /**
     * {@code → FINALIZADO}: {@code EM_ATENDIMENTO} só com ATENDIDO; {@code A_CAMINHO} só com DESISTENCIA;
     * {@code AGUARDANDO} com DESISTENCIA ou ENCAMINHADO.
     */
    public void encerrar(MotivoSaida motivo, OffsetDateTime agora) {
        Objects.requireNonNull(motivo);
        boolean permitido = switch (status) {
            case A_CAMINHO -> motivo == MotivoSaida.DESISTENCIA;
            case AGUARDANDO -> motivo != MotivoSaida.ATENDIDO;
            case EM_ATENDIMENTO -> motivo == MotivoSaida.ATENDIDO;
            case FINALIZADO -> false;
        };
        if (!permitido) {
            throw new TransicaoInvalidaException(status + " -> FINALIZADO (" + motivo + ") não é permitido");
        }
        this.status = StatusFila.FINALIZADO;
        this.motivoSaida = motivo;
    }

    public boolean isAtivo() {
        return status != StatusFila.FINALIZADO;
    }

    private void exigirStatus(StatusFila esperado, StatusFila destino) {
        if (status != esperado) {
            throw new TransicaoInvalidaException(status + " -> " + destino + " não é permitido");
        }
    }

    public String getId() { return id; }
    public String getPacienteId() { return pacienteId; }
    public String getUnidadeId() { return unidadeId; }
    public Prioridade getPrioridade() { return prioridade; }
    public StatusFila getStatus() { return status; }
    public List<String> getSintomas() { return sintomas; }
    public Integer getTempoEstimadoChegadaMin() { return tempoEstimadoChegadaMin; }
    public OffsetDateTime getHorarioAviso() { return horarioAviso; }
    public OffsetDateTime getHorarioChegada() { return horarioChegada; }
    public OffsetDateTime getHorarioInicioAtendimento() { return horarioInicioAtendimento; }
    public MotivoSaida getMotivoSaida() { return motivoSaida; }
}
