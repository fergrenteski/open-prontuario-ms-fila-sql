package br.com.openprontuario.fila.api.fila;

import br.com.openprontuario.fila.domain.model.Prioridade;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** {@code justificativa} é aceita pelo contrato mas não é persistida (o RegistroFila não tem esse campo). */
public record AlterarPrioridadeRequest(@NotNull Prioridade prioridade, @Size(max = 500) String justificativa) {
}
