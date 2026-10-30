package br.com.openprontuario.fila.application.unidade;

import br.com.openprontuario.fila.domain.model.TipoUnidade;

/** Dados editáveis de uma unidade (criação e atualização). */
public record DadosUnidadeCommand(String nome, TipoUnidade tipo, double latitude, double longitude) {
}
