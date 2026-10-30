package br.com.openprontuario.fila.bootstrap;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.AutoConfigurationPackage;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Composition root do MS Fila. Fica em {@code bootstrap}, mas varre o pacote raiz para
 * encontrar controllers (api), adapters (infrastructure) e as configurações de composição.
 * {@link AutoConfigurationPackage} faz o Spring Data/JPA enxergar entidades e repositórios
 * fora do pacote desta classe.
 */
@SpringBootApplication(scanBasePackages = "br.com.openprontuario.fila")
@AutoConfigurationPackage(basePackages = "br.com.openprontuario.fila")
public class OpenProntuarioFilaApplication {

    public static void main(String[] args) {
        SpringApplication.run(OpenProntuarioFilaApplication.class, args);
    }
}
