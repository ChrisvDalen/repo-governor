package io.repogovernor.core.rules;

import io.repogovernor.core.domain.Category;
import io.repogovernor.core.domain.Severity;
import io.repogovernor.core.rules.angular.LegacyNgModuleRule;
import io.repogovernor.core.rules.angular.SignalUsageRule;
import io.repogovernor.core.rules.api.OpenApiSpecRule;
import io.repogovernor.core.rules.cicd.PipelineTestStepRule;
import io.repogovernor.core.rules.docs.ReadmeSectionsRule;
import io.repogovernor.core.rules.java.BuildDependencyRule;
import io.repogovernor.core.rules.java.JavaTestsExistRule;
import io.repogovernor.core.rules.security.EnvFileCommittedRule;
import io.repogovernor.core.rules.security.SecretsInConfigRule;
import io.repogovernor.core.rules.spring.ControllerUsesRepositoryRule;
import io.repogovernor.core.rules.spring.SpringConfigExistsRule;

import java.util.List;

/**
 * The default Repo Governor rule set.
 */
public final class Rules {

    private Rules() {
    }

    public static List<Rule> defaultRules() {
        return List.of(
                // Repo health
                new FileExistsRule("repo.readme.exists", "README.md is present",
                        "Every repository needs a README as its entry point.",
                        Category.REPO_HEALTH, Severity.MAJOR,
                        "Add a README.md describing what the project does and how to use it.",
                        List.of("README.md")),
                new FileExistsRule("repo.gitignore.exists", ".gitignore is present",
                        "A .gitignore prevents build output and local files from being committed.",
                        Category.REPO_HEALTH, Severity.MAJOR,
                        "Add a .gitignore tailored to the technologies in this repository.",
                        List.of(".gitignore")),
                new FileExistsRule("repo.codeowners.exists", "CODEOWNERS is present",
                        "CODEOWNERS makes review responsibility explicit.",
                        Category.REPO_HEALTH, Severity.MINOR,
                        "Add a CODEOWNERS file (root, .github/ or docs/) mapping paths to owners.",
                        List.of("CODEOWNERS", ".github/CODEOWNERS", "docs/CODEOWNERS")),
                new FileExistsRule("repo.license.exists", "LICENSE is present",
                        "A license clarifies how the code may be used.",
                        Category.REPO_HEALTH, Severity.MINOR,
                        "Add a LICENSE file.",
                        List.of("LICENSE", "LICENSE.md", "LICENSE.txt")),
                new FileExistsRule("repo.governor-config.exists", "repo-governor.yml is present",
                        "A repo-governor.yml pins project metadata and rule configuration.",
                        Category.REPO_HEALTH, Severity.INFO,
                        "Run 'repo-governor init' to generate a repo-governor.yml.",
                        List.of("repo-governor.yml")),

                // Java
                new JavaTestsExistRule(),
                new BuildDependencyRule("java.junit.exists", "JUnit dependency is declared", "junit",
                        Severity.MAJOR, "Add JUnit 5 (org.junit.jupiter:junit-jupiter) as a test dependency."),
                new BuildDependencyRule("java.archunit.exists", "ArchUnit dependency is declared", "archunit",
                        Severity.MINOR, "Add ArchUnit (com.tngtech.archunit:archunit-junit5) to guard architecture rules."),

                // Spring Boot
                new SpringConfigExistsRule(),
                new ControllerUsesRepositoryRule(),

                // Angular
                new FileExistsRule("angular.config.exists", "angular.json is present",
                        "An Angular workspace needs an angular.json.",
                        Category.ANGULAR, Severity.MAJOR,
                        "Restore angular.json or regenerate the workspace with the Angular CLI.",
                        List.of("angular.json"), RepoTraits::isAngular),
                new FileExistsRule("angular.src-app.exists", "src/app folder is present",
                        "Angular application code is expected under src/app.",
                        Category.ANGULAR, Severity.MAJOR,
                        "Follow the Angular CLI layout with application code under src/app.",
                        List.of("src/app"), RepoTraits::isAngular),
                new LegacyNgModuleRule(),
                new SignalUsageRule(),

                // API contract
                new OpenApiSpecRule(),

                // CI/CD
                new FileExistsRule("cicd.pipeline.exists", "CI pipeline is configured",
                        "Every repository should be built and tested by a CI pipeline.",
                        Category.CI_CD, Severity.MAJOR,
                        "Add a CI pipeline (.github/workflows, azure-pipelines.yml or .gitlab-ci.yml).",
                        List.of(".github/workflows", "azure-pipelines.yml", ".gitlab-ci.yml")),
                new PipelineTestStepRule(),

                // Security
                new FileExistsRule("security.dependency-updates.configured", "Automated dependency updates are configured",
                        "Dependabot or Renovate keeps dependencies patched.",
                        Category.SECURITY, Severity.MAJOR,
                        "Add .github/dependabot.yml or a renovate.json configuration.",
                        List.of(".github/dependabot.yml", "renovate.json", ".renovaterc", ".renovaterc.json")),
                new EnvFileCommittedRule(),
                new SecretsInConfigRule(),

                // AI readiness
                new FileExistsRule("ai.copilot-instructions.exists", "Copilot instructions are present",
                        "AI coding assistants work better with repository-specific instructions.",
                        Category.AI_READINESS, Severity.MINOR,
                        "Add .github/copilot-instructions.md describing conventions and architecture.",
                        List.of(".github/copilot-instructions.md", ".github/instructions")),
                new FileExistsRule("ai.agents-file.exists", "Agent instructions are present",
                        "An AGENTS.md (or CLAUDE.md) helps AI agents navigate the repository.",
                        Category.AI_READINESS, Severity.MINOR,
                        "Add an AGENTS.md with build, test and convention notes for AI agents.",
                        List.of("AGENTS.md", "CLAUDE.md", ".github/agents")),

                // Documentation
                new FileExistsRule("docs.folder.exists", "docs folder is present",
                        "Larger design documentation belongs in a docs/ folder.",
                        Category.DOCUMENTATION, Severity.INFO,
                        "Add a docs/ folder for architecture and design documentation.",
                        List.of("docs")),
                new ReadmeSectionsRule());
    }
}
