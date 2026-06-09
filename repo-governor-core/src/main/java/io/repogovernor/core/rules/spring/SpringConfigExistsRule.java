package io.repogovernor.core.rules.spring;

import io.repogovernor.core.domain.Category;
import io.repogovernor.core.domain.Finding;
import io.repogovernor.core.domain.Severity;
import io.repogovernor.core.rules.RepoTraits;
import io.repogovernor.core.rules.Rule;
import io.repogovernor.core.scanner.RepoFiles;

import java.util.List;

public final class SpringConfigExistsRule implements Rule {

    @Override
    public String id() {
        return "spring.config.exists";
    }

    @Override
    public String title() {
        return "Spring Boot configuration file is present";
    }

    @Override
    public String description() {
        return "A Spring Boot application should have an application.yml or application.properties.";
    }

    @Override
    public Category category() {
        return Category.SPRING_BOOT;
    }

    @Override
    public Severity severity() {
        return Severity.MINOR;
    }

    @Override
    public boolean appliesTo(RepoFiles repo) {
        return RepoTraits.isSpringBoot(repo);
    }

    @Override
    public List<Finding> evaluate(RepoFiles repo) {
        boolean found = repo.allFiles().stream().anyMatch(f -> {
            String name = f.substring(f.lastIndexOf('/') + 1);
            return f.contains("src/main/resources/")
                    && (name.startsWith("application") && (name.endsWith(".yml")
                    || name.endsWith(".yaml") || name.endsWith(".properties")));
        });
        if (found) {
            return List.of();
        }
        return List.of(Finding.builder(id())
                .title(title())
                .description("No application.yml, application.yaml or application.properties found under src/main/resources.")
                .severity(severity())
                .category(category())
                .recommendation("Add an application.yml under src/main/resources with explicit configuration.")
                .build());
    }
}
