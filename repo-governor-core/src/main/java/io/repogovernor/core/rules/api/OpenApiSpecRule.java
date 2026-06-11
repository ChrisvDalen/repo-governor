package io.repogovernor.core.rules.api;

import io.repogovernor.core.domain.Category;
import io.repogovernor.core.domain.Finding;
import io.repogovernor.core.domain.Severity;
import io.repogovernor.core.rules.RepoTraits;
import io.repogovernor.core.rules.Rule;
import io.repogovernor.core.scanner.RepoFiles;
import io.repogovernor.core.scanner.TechnologyDetector;

import java.util.List;

/**
 * Spring Boot services should publish an OpenAPI contract.
 */
public final class OpenApiSpecRule implements Rule {

    @Override
    public String id() {
        return "api.openapi.exists";
    }

    @Override
    public String title() {
        return "OpenAPI specification is present";
    }

    @Override
    public String description() {
        return "Services should describe their API with an openapi.yml, openapi.yaml or openapi.json contract.";
    }

    @Override
    public Category category() {
        return Category.API_CONTRACT;
    }

    @Override
    public Severity severity() {
        return Severity.MAJOR;
    }

    @Override
    public boolean appliesTo(RepoFiles repo) {
        return RepoTraits.isSpringBoot(repo) || TechnologyDetector.hasOpenApiSpec(repo);
    }

    @Override
    public List<Finding> evaluate(RepoFiles repo) {
        if (TechnologyDetector.hasOpenApiSpec(repo)) {
            return List.of();
        }
        return List.of(Finding.builder(id())
                .title(title())
                .description("No openapi.yml, openapi.yaml or openapi.json found anywhere in the repository.")
                .severity(severity())
                .category(category())
                .recommendation("Adopt contract-first API design: add an OpenAPI 3 spec and validate it in CI.")
                .build());
    }
}
