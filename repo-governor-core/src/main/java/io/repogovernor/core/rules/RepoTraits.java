package io.repogovernor.core.rules;

import io.repogovernor.core.scanner.RepoFiles;
import io.repogovernor.core.scanner.TechnologyDetector;

/**
 * Shared technology checks used by rules to decide whether they apply.
 */
public final class RepoTraits {

    private RepoTraits() {
    }

    public static boolean isJava(RepoFiles repo) {
        return repo.allFiles().stream().anyMatch(f ->
                f.endsWith("pom.xml") || f.endsWith("build.gradle") || f.endsWith("build.gradle.kts"));
    }

    public static boolean isSpringBoot(RepoFiles repo) {
        return isJava(repo) && TechnologyDetector.buildFilesContain(repo, "spring-boot");
    }

    public static boolean isAngular(RepoFiles repo) {
        return repo.exists("angular.json")
                || repo.read("package.json").map(c -> c.contains("@angular/core")).orElse(false);
    }
}
