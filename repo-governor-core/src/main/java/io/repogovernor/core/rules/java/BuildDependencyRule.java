package io.repogovernor.core.rules.java;

import io.repogovernor.core.domain.Category;
import io.repogovernor.core.domain.Finding;
import io.repogovernor.core.domain.Severity;
import io.repogovernor.core.rules.RepoTraits;
import io.repogovernor.core.rules.Rule;
import io.repogovernor.core.scanner.RepoFiles;
import io.repogovernor.core.scanner.TechnologyDetector;

import java.util.List;

/**
 * Fails when none of the Maven/Gradle build files mention the given
 * dependency token (e.g. {@code junit}, {@code archunit}).
 */
public final class BuildDependencyRule implements Rule {

    private final String id;
    private final String title;
    private final String dependencyToken;
    private final Severity severity;
    private final String recommendation;

    public BuildDependencyRule(String id, String title, String dependencyToken,
                               Severity severity, String recommendation) {
        this.id = id;
        this.title = title;
        this.dependencyToken = dependencyToken;
        this.severity = severity;
        this.recommendation = recommendation;
    }

    @Override
    public String id() {
        return id;
    }

    @Override
    public String title() {
        return title;
    }

    @Override
    public String description() {
        return "Build files should declare a dependency containing '" + dependencyToken + "'.";
    }

    @Override
    public Category category() {
        return Category.JAVA;
    }

    @Override
    public Severity severity() {
        return severity;
    }

    @Override
    public boolean appliesTo(RepoFiles repo) {
        return RepoTraits.isJava(repo);
    }

    @Override
    public List<Finding> evaluate(RepoFiles repo) {
        if (TechnologyDetector.buildFilesContain(repo, dependencyToken)) {
            return List.of();
        }
        return List.of(Finding.builder(id)
                .title(title)
                .description("No build file (pom.xml, build.gradle) declares a dependency containing '"
                        + dependencyToken + "'.")
                .severity(severity)
                .category(category())
                .recommendation(recommendation)
                .build());
    }
}
