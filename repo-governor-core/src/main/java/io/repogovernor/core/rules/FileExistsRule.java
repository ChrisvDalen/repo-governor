package io.repogovernor.core.rules;

import io.repogovernor.core.domain.Category;
import io.repogovernor.core.domain.Finding;
import io.repogovernor.core.domain.Severity;
import io.repogovernor.core.scanner.RepoFiles;

import java.util.List;
import java.util.function.Predicate;

/**
 * Rule that fails when none of the candidate paths exist in the repository.
 */
public final class FileExistsRule implements Rule {

    private final String id;
    private final String title;
    private final String description;
    private final Category category;
    private final Severity severity;
    private final String recommendation;
    private final List<String> candidatePaths;
    private final Predicate<RepoFiles> appliesTo;

    public FileExistsRule(String id,
                          String title,
                          String description,
                          Category category,
                          Severity severity,
                          String recommendation,
                          List<String> candidatePaths) {
        this(id, title, description, category, severity, recommendation, candidatePaths, repo -> true);
    }

    public FileExistsRule(String id,
                          String title,
                          String description,
                          Category category,
                          Severity severity,
                          String recommendation,
                          List<String> candidatePaths,
                          Predicate<RepoFiles> appliesTo) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.category = category;
        this.severity = severity;
        this.recommendation = recommendation;
        this.candidatePaths = List.copyOf(candidatePaths);
        this.appliesTo = appliesTo;
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
        return description;
    }

    @Override
    public Category category() {
        return category;
    }

    @Override
    public Severity severity() {
        return severity;
    }

    @Override
    public boolean appliesTo(RepoFiles repo) {
        return appliesTo.test(repo);
    }

    @Override
    public List<Finding> evaluate(RepoFiles repo) {
        boolean found = candidatePaths.stream().anyMatch(repo::exists);
        if (found) {
            return List.of();
        }
        return List.of(Finding.builder(id)
                .title(title)
                .description(description + " Expected one of: " + String.join(", ", candidatePaths) + ".")
                .severity(severity)
                .category(category)
                .recommendation(recommendation)
                .build());
    }
}
