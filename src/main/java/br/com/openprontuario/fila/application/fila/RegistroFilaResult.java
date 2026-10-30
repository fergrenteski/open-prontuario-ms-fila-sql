package br.com.openprontuario.fila.application.fila;

import br.com.openprontuario.fila.domain.model.RegistroFila;

/** Registro de fila com o nome de exibição do paciente (pode ser nulo). */
public record RegistroFilaResult(RegistroFila registro, String pacienteNome) {
}
