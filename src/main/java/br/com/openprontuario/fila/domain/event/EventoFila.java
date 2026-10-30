package br.com.openprontuario.fila.domain.event;

/** Tipos de evento publicados a cada transição da máquina de estados (arc42 §8.4). */
public enum EventoFila {
    PACIENTE_A_CAMINHO, PACIENTE_CHEGOU, PRIORIDADE_ALTERADA, ATENDIMENTO_INICIADO, PACIENTE_REMOVIDO
}
