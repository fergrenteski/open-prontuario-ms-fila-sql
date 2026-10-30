package br.com.openprontuario.fila.infrastructure.persistence;

import br.com.openprontuario.fila.bootstrap.OpenProntuarioFilaApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.MSSQLServerContainer;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;

/**
 * Sobe um SQL Server 2022 real (Testcontainers) uma única vez para todos os *IT.
 * A imagem só existe para amd64; em Apple Silicon roda emulada.
 */
@SpringBootTest(classes = OpenProntuarioFilaApplication.class, properties = "INTERNAL_API_KEY=chave-it")
public abstract class IntegracaoBaseIT {

    @ServiceConnection
    static final MSSQLServerContainer<?> MSSQL =
            new MSSQLServerContainer<>(DockerImageName.parse("mcr.microsoft.com/mssql/server:2022-latest"))
                    .acceptLicense()
                    .withCreateContainerCmdModifier(cmd -> cmd.withPlatform("linux/amd64"))
                    .withStartupTimeout(Duration.ofMinutes(5));

    static {
        MSSQL.start();
    }
}
