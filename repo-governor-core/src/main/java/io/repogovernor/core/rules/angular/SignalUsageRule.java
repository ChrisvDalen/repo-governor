package io.repogovernor.core.rules.angular;

import io.repogovernor.core.domain.Category;
import io.repogovernor.core.domain.Finding;
import io.repogovernor.core.domain.Severity;
import io.repogovernor.core.rules.RepoTraits;
import io.repogovernor.core.rules.Rule;
import io.repogovernor.core.scanner.RepoFiles;

import java.util.List;

/**
 * Informational rule: reports when no Angular signal usage is detected.
 */
public final class SignalUsageRule implements Rule {

    @Override
    public String id() {
        return "angular.signals.used";
    }

    @Override
    public String title() {
        return "Angular signals are used for state";
    }

    @Override
    public String description() {
        return "Angular projects should manage local state with signals.";
    }

    @Override
    public Category category() {
        return Category.ANGULAR;
    }

    @Override
    public Severity severity() {
        return Severity.INFO;
    }

    @Override
    public boolean appliesTo(RepoFiles repo) {
        return RepoTraits.isAngular(repo);
    }

    @Override
    public List<Finding> evaluate(RepoFiles repo) {
        boolean signalsUsed = repo.allFiles().stream()
                .filter(f -> f.endsWith(".ts") && !f.endsWith(".spec.ts"))
                .map(f -> repo.read(f).orElse(""))
                .anyMatch(c -> c.contains("signal(") || c.contains("computed(") || c.contains("input(")
                        || c.contains("toSignal("));
        if (signalsUsed) {
            return List.of();
        }
        return List.of(Finding.builder(id())
                .title(title())
                .description("No signal(), computed(), input() or toSignal() usage detected in TypeScript sources.")
                .severity(severity())
                .category(category())
                .recommendation("Adopt Angular signals for component state instead of mutable fields or ad-hoc RxJS.")
                .build());
    }
}
