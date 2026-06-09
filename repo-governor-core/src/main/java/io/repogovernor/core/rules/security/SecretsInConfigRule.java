package io.repogovernor.core.rules.security;

import io.repogovernor.core.domain.Category;
import io.repogovernor.core.domain.Finding;
import io.repogovernor.core.domain.Severity;
import io.repogovernor.core.rules.Rule;
import io.repogovernor.core.scanner.RepoFiles;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Heuristic scan for hard-coded secret-looking values in configuration files.
 * Placeholders such as {@code ${DB_PASSWORD}} are ignored.
 */
public final class SecretsInConfigRule implements Rule {

    private static final Pattern SECRET_LINE = Pattern.compile(
            "(?i)\\b(password|passwd|secret|api[-_]?key|access[-_]?token)\\b\\s*[:=]\\s*['\"]?([^\\s'\"${][^\\s'\"]*)");

    @Override
    public String id() {
        return "security.secrets.in-config";
    }

    @Override
    public String title() {
        return "No hard-coded secrets in configuration files";
    }

    @Override
    public String description() {
        return "Configuration files should reference secrets via environment variables or a secret manager, not literals.";
    }

    @Override
    public Category category() {
        return Category.SECURITY;
    }

    @Override
    public Severity severity() {
        return Severity.MAJOR;
    }

    @Override
    public List<Finding> evaluate(RepoFiles repo) {
        List<Finding> findings = new ArrayList<>();
        repo.allFiles().stream()
                .filter(SecretsInConfigRule::isConfigFile)
                .forEach(file -> {
                    String[] lines = repo.read(file).orElse("").split("\n", -1);
                    for (int i = 0; i < lines.length; i++) {
                        if (SECRET_LINE.matcher(lines[i]).find()) {
                            findings.add(Finding.builder(id())
                                    .title(title())
                                    .description("Line " + (i + 1) + " of '" + file
                                            + "' looks like a hard-coded secret.")
                                    .severity(severity())
                                    .category(category())
                                    .filePath(file)
                                    .lineNumber(i + 1)
                                    .recommendation("Replace the literal with an environment variable placeholder and rotate the value if it was real.")
                                    .build());
                        }
                    }
                });
        return findings;
    }

    private static boolean isConfigFile(String path) {
        String name = path.substring(path.lastIndexOf('/') + 1).toLowerCase();
        return (name.startsWith("application") || name.startsWith("bootstrap"))
                && (name.endsWith(".yml") || name.endsWith(".yaml") || name.endsWith(".properties"));
    }
}
