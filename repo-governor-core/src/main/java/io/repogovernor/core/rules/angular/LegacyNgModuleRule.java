package io.repogovernor.core.rules.angular;

import io.repogovernor.core.domain.Category;
import io.repogovernor.core.domain.Finding;
import io.repogovernor.core.domain.Severity;
import io.repogovernor.core.rules.RepoTraits;
import io.repogovernor.core.rules.Rule;
import io.repogovernor.core.scanner.RepoFiles;

import java.util.List;

/**
 * Warns when an Angular project still uses NgModules or explicitly opts out of
 * standalone components.
 */
public final class LegacyNgModuleRule implements Rule {

    @Override
    public String id() {
        return "angular.standalone.components";
    }

    @Override
    public String title() {
        return "Angular project uses standalone components";
    }

    @Override
    public String description() {
        return "Modern Angular projects should use standalone components instead of NgModules.";
    }

    @Override
    public Category category() {
        return Category.ANGULAR;
    }

    @Override
    public Severity severity() {
        return Severity.MINOR;
    }

    @Override
    public boolean appliesTo(RepoFiles repo) {
        return RepoTraits.isAngular(repo);
    }

    @Override
    public List<Finding> evaluate(RepoFiles repo) {
        return repo.allFiles().stream()
                .filter(f -> f.endsWith(".ts") && !f.endsWith(".spec.ts"))
                .filter(f -> {
                    String content = repo.read(f).orElse("");
                    return content.contains("@NgModule") || content.contains("standalone: false");
                })
                .map(f -> Finding.builder(id())
                        .title(title())
                        .description("File '" + f + "' uses @NgModule or standalone: false.")
                        .severity(severity())
                        .category(category())
                        .filePath(f)
                        .recommendation("Migrate to standalone components (ng generate @angular/core:standalone).")
                        .build())
                .toList();
    }
}
