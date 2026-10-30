package br.com.openprontuario.fila.application.fila.ports;

import br.com.openprontuario.fila.domain.model.RegistroFila;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

/** Porta de saída para persistência dos registros de fila. */
public interface RegistroFilaRepository {

    /**
     * Grava o registro. Lança {@code RegistroAtivoDuplicadoException} se violar a regra de
     * um registro ativo por paciente/unidade (garantida também por índice único no banco).
     */
    RegistroFila salvar(RegistroFila registro);

    Optional<RegistroFila> buscarPorId(String id);

    /** Registros com status diferente de FINALIZADO. */
    List<RegistroFila> listarAtivosPorUnidade(String unidadeId);

    boolean existeAtivo(String pacienteId, String unidadeId);

    /** Ativos da unidade ou com início de atendimento a partir de {@code inicioAtendimentoDesde}. */
    List<RegistroFila> listarParaSnapshot(String unidadeId, OffsetDateTime inicioAtendimentoDesde);
}
