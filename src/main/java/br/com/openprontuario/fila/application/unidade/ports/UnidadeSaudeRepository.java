package br.com.openprontuario.fila.application.unidade.ports;

import br.com.openprontuario.fila.domain.model.TipoUnidade;
import br.com.openprontuario.fila.domain.model.UnidadeSaude;

import java.util.List;
import java.util.Optional;

/** Porta de saída para persistência de unidades de saúde. */
public interface UnidadeSaudeRepository {

    UnidadeSaude salvar(UnidadeSaude unidade);

    Optional<UnidadeSaude> buscarPorId(String id);

    /** Todas as unidades, ou somente as do tipo informado quando {@code tipo != null}. */
    List<UnidadeSaude> listar(TipoUnidade tipo);

    /** Unidades em que o usuário tem lotação. */
    List<UnidadeSaude> listarPorUsuarioLotado(String usuarioId);

    /** {@code true} se a unidade tem lotações ou registros de fila. */
    boolean emUso(String id);

    void remover(String id);
}
