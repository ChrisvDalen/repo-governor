package io.repogovernor.core.rules.docs;

import io.repogovernor.core.domain.Category;
import io.repogovernor.core.domain.Finding;
import io.repogovernor.core.domain.Severity;
import io.repogovernor.core.rules.Rule;
import io.repogovernor.core.scanner.RepoFiles;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Checks that the README explains how to install, run and test the project.
 */
public final class ReadmeSectionsRule implements Rule {

    private static final Map<String, List<String>> EXPECTED_SECTIONS = new LinkedHashMap<>();

    static {
        EXPECTED_SECTIONS.put("installation", List.of("install", "installation", "setup", "getting started", "prerequisites"));
        EXPECTED_SECTIONS.put("run", List.of("run", "running", "start", "usage", "quick start"));
        EXPECTED_SECTIONS.put("test", List.of("test", "testing"));
    }

    @Override
    public String id() {
        return "docs.readme.sections";
    }

    @Override
    public String title() {
        return "README explains install, run and test";
    }

    @Override
    public String description() {
        return "The README should contain installation, run and test instructions.";
    }

    @Override
    public Category category() {
        return Category.DOCUMENTATION;
    }

    @Override
    public Severity severity() {
        return Severity.MINOR;
    }

    @Override
    public boolean appliesTo(RepoFiles repo) {
        return repo.exists("README.md");
    }

    @Override
    public List<Finding> evaluate(RepoFiles repo) {
        String readme = repo.read("README.md").orElse("").toLowerCase();
        List<Finding> findings = new ArrayList<>();
        EXPECTED_SECTIONS.forEach((section, keywords) -> {
            boolean present = keywords.stream().anyMatch(readme::contains);
            if (!present) {
                findings.add(Finding.builder(id())
                        .title(title())
                        .description("README.md does not appear to contain " + section + " instructions.")
                        .severity(severity())
                        .category(category())
                        .filePath("README.md")
                        .recommendation("Add a '" + section + "' section to README.md.")
                        .build());
            }
        });
        return findings;
    }
}
