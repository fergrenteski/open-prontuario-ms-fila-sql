package br.com.openprontuario.fila.application.fila.avisarchegada;

import java.util.List;

/** {@code pacienteId} e {@code pacienteNome} vêm da identidade autenticada (X-User-Id / X-User-Name). */
public record AvisarChegadaCommand(String pacienteId, String pacienteNome, String unidadeId, List<String> sintomas,
                                   Integer tempoEstimadoChegadaMin) {
}
