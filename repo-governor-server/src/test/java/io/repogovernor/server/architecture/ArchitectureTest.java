package io.repogovernor.server.architecture;

import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

@AnalyzeClasses(packages = "io.repogovernor.server")
class ArchitectureTest {

    @ArchTest
    static final ArchRule controllersDoNotUseSpringDataRepositories = noClasses()
            .that().areAnnotatedWith(org.springframework.web.bind.annotation.RestController.class)
            .should().dependOnClassesThat().areAssignableTo(org.springframework.data.repository.Repository.class)
            .because("controllers must go through services, not repositories");

    @ArchTest
    static final ArchRule controllersDoNotReturnEntities = noClasses()
            .that().areAnnotatedWith(org.springframework.web.bind.annotation.RestController.class)
            .should().dependOnClassesThat().areAnnotatedWith(jakarta.persistence.Entity.class)
            .because("API boundaries use DTOs, not JPA entities");

    @ArchTest
    static final ArchRule serverDoesNotDependOnCliOrCore = noClasses()
            .should().dependOnClassesThat().resideInAnyPackage("io.repogovernor.cli..", "io.repogovernor.core..")
            .because("the server is independent of the CLI; the JSON contract is the only coupling");

    @ArchTest
    static final ArchRule entitiesLiveInFeaturePackages = classes()
            .that().areAnnotatedWith(jakarta.persistence.Entity.class)
            .should().haveSimpleNameEndingWith("Entity")
            .because("entities are named explicitly so DTO/entity confusion is visible");
}
