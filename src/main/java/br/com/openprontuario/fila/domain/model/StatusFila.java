package br.com.openprontuario.fila.domain.model;

/** Estados do ciclo de vida do RegistroFila (arc42 §6.11). */
public enum StatusFila {
    A_CAMINHO, AGUARDANDO, EM_ATENDIMENTO, FINALIZADO
}
