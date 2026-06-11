package io.repogovernor.core.rules;

import io.repogovernor.core.domain.Finding;
import io.repogovernor.core.domain.Severity;
import io.repogovernor.core.rules.angular.LegacyNgModuleRule;
import io.repogovernor.core.rules.api.OpenApiSpecRule;
import io.repogovernor.core.rules.cicd.PipelineTestStepRule;
import io.repogovernor.core.rules.docs.ReadmeSectionsRule;
import io.repogovernor.core.rules.java.BuildDependencyRule;
import io.repogovernor.core.rules.java.JavaTestsExistRule;
import io.repogovernor.core.rules.security.EnvFileCommittedRule;
import io.repogovernor.core.rules.security.SecretsInConfigRule;
import io.repogovernor.core.rules.spring.ControllerUsesRepositoryRule;
import io.repogovernor.core.rules.spring.SpringConfigExistsRule;
import io.repogovernor.core.testsupport.InMemoryRepoFiles;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DefaultRulesTest {

    private static final String SPRING_POM = """
            <project><dependencies>
              <dependency><groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-starter-webmvc</artifactId></dependency>
            </dependencies></project>
            """;

    private static Rule rule(String id) {
        return Rules.defaultRules().stream()
                .filter(r -> r.id().equals(id))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown rule " + id));
    }

    @Test
    void readmeRuleFailsWhenReadmeMissing() {
        var repo = InMemoryRepoFiles.repo().withFile(".gitignore");

        List<Finding> findings = rule("repo.readme.exists").evaluate(repo);

        assertThat(findings).hasSize(1);
        assertThat(findings.getFirst().severity()).isEqualTo(Severity.MAJOR);
        assertThat(findings.getFirst().ruleId()).isEqualTo("repo.readme.exists");
    }

    @Test
    void readmeRulePassesWhenReadmePresent() {
        var repo = InMemoryRepoFiles.repo().withFile("README.md", "# hi");

        assertThat(rule("repo.readme.exists").evaluate(repo)).isEmpty();
    }

    @Test
    void gitignoreRuleFailsWhenMissing() {
        var repo = InMemoryRepoFiles.repo().withFile("README.md", "# hi");

        assertThat(rule("repo.gitignore.exists").evaluate(repo)).hasSize(1);
    }

    @Test
    void codeownersRuleAcceptsGithubLocation() {
        var repo = InMemoryRepoFiles.repo().withFile(".github/CODEOWNERS", "* @team");

        assertThat(rule("repo.codeowners.exists").evaluate(repo)).isEmpty();
    }

    @Test
    void javaTestsRuleOnlyAppliesToJavaProjects() {
        var nodeRepo = InMemoryRepoFiles.repo().withFile("package.json", "{}");
        var javaRepo = InMemoryRepoFiles.repo().withFile("pom.xml", "<project/>");

        var ruleUnderTest = new JavaTestsExistRule();
        assertThat(ruleUnderTest.appliesTo(nodeRepo)).isFalse();
        assertThat(ruleUnderTest.appliesTo(javaRepo)).isTrue();
        assertThat(ruleUnderTest.evaluate(javaRepo)).hasSize(1);
    }

    @Test
    void javaTestsRulePassesWhenTestSourcesExist() {
        var repo = InMemoryRepoFiles.repo()
                .withFile("pom.xml", "<project/>")
                .withFile("src/test/java/FooTest.java", "class FooTest {}");

        assertThat(new JavaTestsExistRule().evaluate(repo)).isEmpty();
    }

    @Test
    void junitRuleChecksBuildFiles() {
        var withJunit = InMemoryRepoFiles.repo()
                .withFile("build.gradle", "testImplementation 'org.junit.jupiter:junit-jupiter:5.11.4'");
        var withoutJunit = InMemoryRepoFiles.repo().withFile("build.gradle", "dependencies {}");

        var junitRule = new BuildDependencyRule("java.junit.exists", "JUnit", "junit", Severity.MAJOR, "add it");
        assertThat(junitRule.evaluate(withJunit)).isEmpty();
        assertThat(junitRule.evaluate(withoutJunit)).hasSize(1);
    }

    @Test
    void springConfigRuleRequiresApplicationConfig() {
        var withConfig = InMemoryRepoFiles.repo()
                .withFile("pom.xml", SPRING_POM)
                .withFile("src/main/resources/application.yml", "spring: {}");
        var withoutConfig = InMemoryRepoFiles.repo().withFile("pom.xml", SPRING_POM);

        var springRule = new SpringConfigExistsRule();
        assertThat(springRule.appliesTo(withConfig)).isTrue();
        assertThat(springRule.evaluate(withConfig)).isEmpty();
        assertThat(springRule.evaluate(withoutConfig)).hasSize(1);
    }

    @Test
    void controllerUsingRepositoryIsFlagged() {
        var repo = InMemoryRepoFiles.repo()
                .withFile("pom.xml", SPRING_POM)
                .withFile("src/main/java/demo/UserController.java", """
                        class UserController {
                          private final UserRepository userRepository;
                        }
                        """)
                .withFile("src/main/java/demo/CleanController.java", """
                        class CleanController {
                          private final UserService userService;
                          void load() { userService.listForRepository("x"); }
                        }
                        """);

        List<Finding> findings = new ControllerUsesRepositoryRule().evaluate(repo);

        assertThat(findings).hasSize(1);
        assertThat(findings.getFirst().filePath()).contains("UserController.java");
    }

    @Test
    void ngModuleUsageIsFlagged() {
        var repo = InMemoryRepoFiles.repo()
                .withFile("angular.json", "{}")
                .withFile("src/app/legacy.module.ts", "@NgModule({}) export class LegacyModule {}");

        assertThat(new LegacyNgModuleRule().evaluate(repo)).hasSize(1);
    }

    @Test
    void openApiRulePassesWhenSpecExistsAnywhere() {
        var repo = InMemoryRepoFiles.repo()
                .withFile("pom.xml", SPRING_POM)
                .withFile("contracts/openapi.yaml", "openapi: 3.0.0");

        assertThat(new OpenApiSpecRule().evaluate(repo)).isEmpty();
    }

    @Test
    void pipelineTestStepRuleOnlyAppliesWhenPipelineExists() {
        var noPipeline = InMemoryRepoFiles.repo().withFile("README.md", "");
        var pipelineWithoutTests = InMemoryRepoFiles.repo()
                .withFile(".github/workflows/build.yml", "jobs:\n  build:\n    steps: []");
        var pipelineWithTests = InMemoryRepoFiles.repo()
                .withFile(".github/workflows/ci.yml", "jobs:\n  test:\n    run: ./gradlew test");

        var pipelineRule = new PipelineTestStepRule();
        assertThat(pipelineRule.appliesTo(noPipeline)).isFalse();
        assertThat(pipelineRule.evaluate(pipelineWithoutTests)).hasSize(1);
        assertThat(pipelineRule.evaluate(pipelineWithTests)).isEmpty();
    }

    @Test
    void committedEnvFileIsBlocker() {
        var repo = InMemoryRepoFiles.repo().withFile(".env", "DB_PASSWORD=hunter2");

        List<Finding> findings = new EnvFileCommittedRule().evaluate(repo);

        assertThat(findings).hasSize(1);
        assertThat(findings.getFirst().severity()).isEqualTo(Severity.BLOCKER);
    }

    @Test
    void hardcodedSecretIsFlaggedButPlaceholderIsNot() {
        var repo = InMemoryRepoFiles.repo()
                .withFile("src/main/resources/application.yml", """
                        spring:
                          datasource:
                            password: supersecret123
                        """)
                .withFile("src/main/resources/application-prod.yml", """
                        spring:
                          datasource:
                            password: ${DB_PASSWORD}
                        """);

        List<Finding> findings = new SecretsInConfigRule().evaluate(repo);

        assertThat(findings).hasSize(1);
        assertThat(findings.getFirst().filePath()).isEqualTo("src/main/resources/application.yml");
        assertThat(findings.getFirst().lineNumber()).isEqualTo(3);
    }

    @Test
    void readmeSectionsRuleReportsMissingSections() {
        var complete = InMemoryRepoFiles.repo().withFile("README.md", """
                # Demo
                ## Installation
                ## Running
                ## Testing
                """);
        var incomplete = InMemoryRepoFiles.repo().withFile("README.md", "# Demo");

        var sectionsRule = new ReadmeSectionsRule();
        assertThat(sectionsRule.evaluate(complete)).isEmpty();
        assertThat(sectionsRule.evaluate(incomplete)).hasSize(3);
    }

    @Test
    void dependencyUpdateRuleAcceptsRenovate() {
        var repo = InMemoryRepoFiles.repo().withFile("renovate.json", "{}");

        assertThat(rule("security.dependency-updates.configured").evaluate(repo)).isEmpty();
    }

    @Test
    void defaultRuleSetHasUniqueIds() {
        var ids = Rules.defaultRules().stream().map(Rule::id).toList();

        assertThat(ids).doesNotHaveDuplicates();
        assertThat(ids).hasSizeGreaterThanOrEqualTo(20);
    }
}
