package io.repogovernor.core.rules.security;

import io.repogovernor.core.domain.Category;
import io.repogovernor.core.domain.Finding;
import io.repogovernor.core.domain.Severity;
import io.repogovernor.core.rules.Rule;
import io.repogovernor.core.scanner.RepoFiles;

import java.util.List;

/**
 * A committed .env file very likely contains credentials.
 */
public final class EnvFileCommittedRule implements Rule {

    @Override
    public String id() {
        return "security.env.not-committed";
    }

    @Override
    public String title() {
        return "No .env file committed";
    }

    @Override
    public String description() {
        return ".env files typically contain secrets and must never be committed.";
    }

    @Override
    public Category category() {
        return Category.SECURITY;
    }

    @Override
    public Severity severity() {
        return Severity.BLOCKER;
    }

    @Override
    public List<Finding> evaluate(RepoFiles repo) {
        return repo.allFiles().stream()
                .filter(f -> f.equals(".env") || f.endsWith("/.env"))
                .map(f -> Finding.builder(id())
                        .title(title())
                        .description("A .env file is committed at '" + f + "'.")
                        .severity(severity())
                        .category(category())
                        .filePath(f)
                        .recommendation("Remove the file from git history, rotate any exposed secrets and add .env to .gitignore.")
                        .build())
                .toList();
    }
}
