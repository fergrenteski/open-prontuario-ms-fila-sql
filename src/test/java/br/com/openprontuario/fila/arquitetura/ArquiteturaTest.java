package br.com.openprontuario.fila.arquitetura;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import org.springframework.web.bind.annotation.RestController;

import static com.tngtech.archunit.core.domain.JavaClass.Predicates.resideInAPackage;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.simpleNameEndingWith;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.Architectures.layeredArchitecture;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

/** Regras do arc42 §8.8 adaptadas às camadas domain / application / infrastructure / api / bootstrap. */
@AnalyzeClasses(packages = "br.com.openprontuario.fila", importOptions = ImportOption.DoNotIncludeTests.class)
class ArquiteturaTest {

    private static final String RAIZ = "br.com.openprontuario.fila";
    private static final String DOMAIN = RAIZ + ".domain..";
    private static final String APPLICATION = RAIZ + ".application..";
    private static final String INFRASTRUCTURE = RAIZ + ".infrastructure..";
    private static final String API = RAIZ + ".api..";
    private static final String BOOTSTRAP = RAIZ + ".bootstrap..";
    private static final String PORTS = RAIZ + ".application.*.ports..";
    /** application.<feature>.<casodeuso> */
    private static final String CASOS_DE_USO = RAIZ + ".application.*.*..";

    // 1. Regra de dependência: api e infrastructure não se conhecem; só o bootstrap enxerga tudo
    @ArchTest
    static final ArchRule camadas_respeitam_a_regra_de_dependencia = layeredArchitecture()
            .consideringOnlyDependenciesInLayers()
            .layer("Domain").definedBy(DOMAIN)
            .layer("Application").definedBy(APPLICATION)
            .layer("Infrastructure").definedBy(INFRASTRUCTURE)
            .layer("Api").definedBy(API)
            .layer("Bootstrap").definedBy(BOOTSTRAP)
            .whereLayer("Bootstrap").mayNotBeAccessedByAnyLayer()
            .whereLayer("Api").mayOnlyBeAccessedByLayers("Bootstrap")
            .whereLayer("Infrastructure").mayOnlyBeAccessedByLayers("Bootstrap")
            .whereLayer("Application").mayOnlyBeAccessedByLayers("Api", "Infrastructure", "Bootstrap")
            .whereLayer("Domain").mayOnlyBeAccessedByLayers("Application", "Api", "Infrastructure", "Bootstrap");

    // 2. domain é Java puro
    @ArchTest
    static final ArchRule dominio_eh_java_puro = classes().that().resideInAPackage(DOMAIN)
            .should().onlyDependOnClassesThat().resideInAnyPackage(DOMAIN, "java..")
            .because("o domínio não pode conhecer frameworks nem camadas externas");

    @ArchTest
    static final ArchRule dominio_sem_spring_jpa_nem_azure = noClasses().that().resideInAPackage(DOMAIN)
            .should().dependOnClassesThat().resideInAnyPackage("org.springframework..", "jakarta.persistence..",
                    "org.hibernate..", "com.azure..");

    // 3. application depende só de domain e da biblioteca padrão (casos de uso sem anotações de framework)
    @ArchTest
    static final ArchRule aplicacao_depende_apenas_do_dominio = classes().that().resideInAPackage(APPLICATION)
            .should().onlyDependOnClassesThat().resideInAnyPackage(APPLICATION, DOMAIN, "java..");

    // 4. controllers chamam somente casos de uso (nunca portas, adapters ou helpers internos)
    @ArchTest
    static final ArchRule api_nao_acessa_portas_nem_infraestrutura = noClasses().that().resideInAPackage(API)
            .should().dependOnClassesThat().resideInAnyPackage(PORTS, INFRASTRUCTURE);

    @ArchTest
    static final ArchRule controllers_so_usam_casos_de_uso_da_aplicacao = noClasses()
            .that().areAnnotatedWith(RestController.class)
            .should().dependOnClassesThat(resideInAPackage(APPLICATION).and(DescribedPredicate.not(
                    simpleNameEndingWith("UseCase").or(simpleNameEndingWith("Command"))
                            .or(simpleNameEndingWith("Result")))))
            .because("controllers não contêm regra de negócio: só traduzem HTTP para um caso de uso");

    // 5. convenções de nomes
    @ArchTest
    static final ArchRule pacotes_de_caso_de_uso_contem_UseCase_Command_ou_Result = classes()
            .that().resideInAPackage(CASOS_DE_USO).and().resideOutsideOfPackage(PORTS).and().areTopLevelClasses()
            .should().haveSimpleNameEndingWith("UseCase").orShould().haveSimpleNameEndingWith("Command")
            .orShould().haveSimpleNameEndingWith("Result");

    @ArchTest
    static final ArchRule casos_de_uso_ficam_no_seu_slice = classes().that().haveSimpleNameEndingWith("UseCase")
            .should().resideInAPackage(CASOS_DE_USO);

    @ArchTest
    static final ArchRule portas_sao_interfaces_Repository_ou_Port = classes().that().resideInAPackage(PORTS)
            .should().beInterfaces().andShould().haveSimpleNameEndingWith("Repository")
            .orShould().haveSimpleNameEndingWith("Port");

    @ArchTest
    static final ArchRule controllers_terminam_com_Controller_e_ficam_na_api = classes()
            .that().areAnnotatedWith(RestController.class)
            .should().haveSimpleNameEndingWith("Controller").andShould().resideInAPackage(API);

    @ArchTest
    static final ArchRule implementacoes_de_porta_sao_Adapter_na_infraestrutura = classes()
            .that(implementamUmaPorta()).and().resideOutsideOfPackage(RAIZ + ".application..")
            .should().haveSimpleNameEndingWith("Adapter").andShould().resideInAPackage(INFRASTRUCTURE);

    @ArchTest
    static final ArchRule adapters_implementam_uma_porta = classes().that().haveSimpleNameEndingWith("Adapter")
            .should(com.tngtech.archunit.lang.conditions.ArchConditions.be(implementamUmaPorta()));

    // 6. sem ciclos entre camadas nem entre features
    @ArchTest
    static final ArchRule camadas_sem_ciclos = slices().matching(RAIZ + ".(*)..").should().beFreeOfCycles();

    @ArchTest
    static final ArchRule features_da_aplicacao_sem_ciclos =
            slices().matching(RAIZ + ".application.(*)..").should().beFreeOfCycles();

    private static DescribedPredicate<JavaClass> implementamUmaPorta() {
        return DescribedPredicate.describe("implementam uma porta de application.<feature>.ports",
                c -> !c.isInterface() && c.getAllRawInterfaces().stream()
                        .anyMatch(i -> i.getPackageName().matches(".*\\.application\\.[^.]+\\.ports")));
    }
}
