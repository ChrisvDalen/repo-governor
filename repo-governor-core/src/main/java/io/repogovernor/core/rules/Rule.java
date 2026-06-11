package io.repogovernor.core.rules;

import io.repogovernor.core.domain.Category;
import io.repogovernor.core.domain.Finding;
import io.repogovernor.core.domain.Severity;
import io.repogovernor.core.scanner.RepoFiles;

import java.util.List;

/**
 * A single governance check. Rules inspect the repository through
 * {@link RepoFiles} and return findings; they never print or write anything.
 */
public interface Rule {

    String id();

    String title();

    String description();

    Category category();

    Severity severity();

    /**
     * Evaluates the rule. An empty list means the rule passed.
     */
    List<Finding> evaluate(RepoFiles repo);

    /**
     * Whether this rule applies to the repository at all. Rules for
     * technologies that are not present should return false so they do not
     * pollute the report or the score.
     */
    default boolean appliesTo(RepoFiles repo) {
        return true;
    }
}
