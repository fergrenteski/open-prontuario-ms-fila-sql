package br.com.openprontuario.fila.domain;

import br.com.openprontuario.fila.domain.model.MotivoSaida;
import br.com.openprontuario.fila.domain.model.Prioridade;
import br.com.openprontuario.fila.domain.model.RegistroFila;
import br.com.openprontuario.fila.domain.policy.OrdenacaoFilaPolicy;
import org.junit.jupiter.api.Test;

import java.util.List;

import static br.com.openprontuario.fila.domain.RegistroFilaTest.T0;
import static org.assertj.core.api.Assertions.assertThat;

class OrdenacaoFilaPolicyTest {

    private static RegistroFila aviso(String paciente, Prioridade p, int minutoAviso) {
        return RegistroFila.avisar(paciente, "upa-01", List.of("x"), null, p, T0.plusMinutes(minutoAviso));
    }

    @Test
    void ordenaPorGravidadeEDepoisPeloHorarioMaisAntigo() {
        RegistroFila naoUrgente = aviso("a", Prioridade.NAO_URGENTE, 0);
        RegistroFila urgenteTarde = aviso("b", Prioridade.URGENTE, 20);
        RegistroFila urgenteCedo = aviso("c", Prioridade.URGENTE, 5);
        RegistroFila emergencia = aviso("d", Prioridade.EMERGENCIA, 30);

        assertThat(OrdenacaoFilaPolicy.ordenar(List.of(naoUrgente, urgenteTarde, urgenteCedo, emergencia)))
                .containsExactly(emergencia, urgenteCedo, urgenteTarde, naoUrgente);
    }

    @Test
    void usaHorarioDeChegadaQuandoPresenteEAvisoQuandoACaminho() {
        RegistroFila avisouCedoChegouTarde = aviso("a", Prioridade.URGENTE, 0);
        avisouCedoChegouTarde.confirmarChegada(T0.plusMinutes(50));
        RegistroFila aCaminho = aviso("b", Prioridade.URGENTE, 10);

        assertThat(OrdenacaoFilaPolicy.ordenar(List.of(avisouCedoChegouTarde, aCaminho)))
                .containsExactly(aCaminho, avisouCedoChegouTarde);
    }

    @Test
    void removeFinalizados() {
        RegistroFila ativo = aviso("a", Prioridade.URGENTE, 0);
        RegistroFila finalizado = aviso("b", Prioridade.EMERGENCIA, 0);
        finalizado.encerrar(MotivoSaida.DESISTENCIA, T0.plusMinutes(1));

        assertThat(OrdenacaoFilaPolicy.ordenar(List.of(ativo, finalizado))).containsExactly(ativo);
    }
}
