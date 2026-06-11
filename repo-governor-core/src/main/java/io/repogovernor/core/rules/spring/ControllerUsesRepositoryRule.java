package io.repogovernor.core.rules.spring;

import io.repogovernor.core.domain.Category;
import io.repogovernor.core.domain.Finding;
import io.repogovernor.core.domain.Severity;
import io.repogovernor.core.rules.RepoTraits;
import io.repogovernor.core.rules.Rule;
import io.repogovernor.core.scanner.RepoFiles;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Heuristic layering check: warns when a {@code *Controller.java} file appears
 * to use a {@code *Repository} type directly instead of going through a service.
 */
public final class ControllerUsesRepositoryRule implements Rule {

    // Matches type usages such as 'UserRepository userRepository' or an import
    // of a *Repository type, but not method names like 'listForRepository'.
    private static final Pattern REPOSITORY_USAGE = Pattern.compile("\\b[A-Z]\\w*Repository\\b");

    @Override
    public String id() {
        return "spring.controller.no-direct-repository";
    }

    @Override
    public String title() {
        return "Controllers should not use repositories directly";
    }

    @Override
    public String description() {
        return "Controllers that inject repositories bypass the service layer, which couples web concerns to persistence.";
    }

    @Override
    public Category category() {
        return Category.SPRING_BOOT;
    }

    @Override
    public Severity severity() {
        return Severity.MAJOR;
    }

    @Override
    public boolean appliesTo(RepoFiles repo) {
        return RepoTraits.isSpringBoot(repo);
    }

    @Override
    public List<Finding> evaluate(RepoFiles repo) {
        List<Finding> findings = new ArrayList<>();
        repo.allFiles().stream()
                .filter(f -> f.endsWith("Controller.java") && f.contains("src/main/java/"))
                .forEach(file -> {
                    String content = repo.read(file).orElse("");
                    if (REPOSITORY_USAGE.matcher(content).find()) {
                        findings.add(Finding.builder(id())
                                .title(title())
                                .description("Controller '" + file + "' references a *Repository type directly.")
                                .severity(severity())
                                .category(category())
                                .filePath(file)
                                .recommendation("Introduce a service that owns the repository and let the controller call the service.")
                                .build());
                    }
                });
        return findings;
    }
}
