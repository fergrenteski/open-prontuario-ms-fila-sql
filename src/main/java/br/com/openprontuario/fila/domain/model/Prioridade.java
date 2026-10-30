package br.com.openprontuario.fila.domain.model;

/** Gravidade em ordem decrescente (protocolo de Manchester); a ordem dos valores é usada na ordenação da fila. */
public enum Prioridade {
    EMERGENCIA, MUITO_URGENTE, URGENTE, POUCO_URGENTE, NAO_URGENTE
}
