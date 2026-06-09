package io.repogovernor.core.rules.cicd;

import io.repogovernor.core.domain.Category;
import io.repogovernor.core.domain.Finding;
import io.repogovernor.core.domain.Severity;
import io.repogovernor.core.rules.Rule;
import io.repogovernor.core.scanner.RepoFiles;

import java.util.List;

/**
 * Checks that at least one CI pipeline definition runs tests.
 */
public final class PipelineTestStepRule implements Rule {

    @Override
    public String id() {
        return "cicd.test-step.exists";
    }

    @Override
    public String title() {
        return "CI pipeline runs tests";
    }

    @Override
    public String description() {
        return "The CI pipeline should run the test suite on every change.";
    }

    @Override
    public Category category() {
        return Category.CI_CD;
    }

    @Override
    public Severity severity() {
        return Severity.MAJOR;
    }

    @Override
    public boolean appliesTo(RepoFiles repo) {
        return !pipelineFiles(repo).isEmpty();
    }

    @Override
    public List<Finding> evaluate(RepoFiles repo) {
        boolean runsTests = pipelineFiles(repo).stream()
                .map(f -> repo.read(f).orElse("").toLowerCase())
                .anyMatch(c -> c.contains("test"));
        if (runsTests) {
            return List.of();
        }
        return List.of(Finding.builder(id())
                .title(title())
                .description("Pipeline definitions were found, but none of them appears to run tests.")
                .severity(severity())
                .category(category())
                .recommendation("Add an explicit test step (e.g. './gradlew test' or 'npm test') to the pipeline.")
                .build());
    }

    private static List<String> pipelineFiles(RepoFiles repo) {
        return repo.allFiles().stream()
                .filter(f -> (f.startsWith(".github/workflows/") && (f.endsWith(".yml") || f.endsWith(".yaml")))
                        || f.equals("azure-pipelines.yml")
                        || f.equals(".gitlab-ci.yml"))
                .toList();
    }
}
