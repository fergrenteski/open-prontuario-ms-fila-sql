package br.com.openprontuario.fila.domain;

import br.com.openprontuario.fila.domain.model.Prioridade;
import br.com.openprontuario.fila.domain.policy.ClassificacaoPreliminarPolicy;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

class ClassificacaoPreliminarPolicyTest {

    @ParameterizedTest(name = "{0} -> {1}")
    @CsvSource(delimiter = ';', value = {
            "Parada cardíaca; EMERGENCIA",
            "paciente INCONSCIENTE; EMERGENCIA",
            "Convulsão; EMERGENCIA",
            "hemorragia nasal; EMERGENCIA",
            "Dor no peito; MUITO_URGENTE",
            "falta de ar; MUITO_URGENTE",
            "Desmaio; MUITO_URGENTE",
            "suspeita de AVC; MUITO_URGENTE",
            "Febre alta; URGENTE",
            "vômito; URGENTE",
            "fratura no braço; URGENTE",
            "dor intensa nas costas; URGENTE",
            "febre; POUCO_URGENTE",
            "dor de cabeça; POUCO_URGENTE",
            "Tontura; POUCO_URGENTE",
            "tosse; NAO_URGENTE",
            "coriza|espirros; NAO_URGENTE",
            "tosse|febre|dor no peito; MUITO_URGENTE",
            "febre|convulsão|falta de ar; EMERGENCIA",
            "tontura|febre alta; URGENTE"
    })
    void classificaPeloSintomaMaisGrave(String sintomas, Prioridade esperada) {
        assertThat(ClassificacaoPreliminarPolicy.classificar(Arrays.asList(sintomas.split("\\|"))))
                .isEqualTo(esperada);
    }
}
