package io.repogovernor.core.rules.java;

import io.repogovernor.core.domain.Category;
import io.repogovernor.core.domain.Finding;
import io.repogovernor.core.domain.Severity;
import io.repogovernor.core.rules.RepoTraits;
import io.repogovernor.core.rules.Rule;
import io.repogovernor.core.scanner.RepoFiles;

import java.util.List;

public final class JavaTestsExistRule implements Rule {

    @Override
    public String id() {
        return "java.tests.exist";
    }

    @Override
    public String title() {
        return "Java test sources are present";
    }

    @Override
    public String description() {
        return "A Java project should have a src/test/java folder with tests.";
    }

    @Override
    public Category category() {
        return Category.JAVA;
    }

    @Override
    public Severity severity() {
        return Severity.MAJOR;
    }

    @Override
    public boolean appliesTo(RepoFiles repo) {
        return RepoTraits.isJava(repo);
    }

    @Override
    public List<Finding> evaluate(RepoFiles repo) {
        boolean hasTests = repo.allFiles().stream().anyMatch(f -> f.contains("src/test/java/"));
        if (hasTests) {
            return List.of();
        }
        return List.of(Finding.builder(id())
                .title(title())
                .description("No files found under src/test/java in any module.")
                .severity(severity())
                .category(category())
                .recommendation("Add a src/test/java folder and start with unit tests for the core logic.")
                .build());
    }
}
