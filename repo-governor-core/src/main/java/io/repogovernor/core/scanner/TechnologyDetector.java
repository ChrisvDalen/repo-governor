package io.repogovernor.core.scanner;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Detects which technologies a repository uses, based on marker files and
 * build file contents. Detection drives which rule categories are relevant.
 */
public final class TechnologyDetector {

    public Set<String> detect(RepoFiles repo) {
        Set<String> technologies = new LinkedHashSet<>();

        boolean maven = repo.allFiles().stream().anyMatch(f -> f.endsWith("pom.xml"));
        boolean gradle = repo.allFiles().stream()
                .anyMatch(f -> f.endsWith("build.gradle") || f.endsWith("build.gradle.kts"));
        if (maven) {
            technologies.add("maven");
        }
        if (gradle) {
            technologies.add("gradle");
        }
        if (maven || gradle) {
            technologies.add("java");
        }
        if (buildFilesContain(repo, "spring-boot")) {
            technologies.add("spring-boot");
        }
        if (repo.exists("angular.json")
                || repo.read("package.json").map(c -> c.contains("@angular/core")).orElse(false)) {
            technologies.add("angular");
        }
        if (repo.exists("package.json")) {
            technologies.add("node");
        }
        if (repo.allFiles().stream().anyMatch(f -> f.endsWith(".ts"))) {
            technologies.add("typescript");
        }
        if (repo.existsAny("Dockerfile", "docker-compose.yml", "docker-compose.yaml", "compose.yaml")) {
            technologies.add("docker");
        }
        if (repo.isDirectory(".github/workflows")) {
            technologies.add("github-actions");
        }
        if (hasOpenApiSpec(repo)) {
            technologies.add("openapi");
        }
        return technologies;
    }

    public static boolean buildFilesContain(RepoFiles repo, String token) {
        return repo.allFiles().stream()
                .filter(f -> f.endsWith("pom.xml") || f.endsWith("build.gradle") || f.endsWith("build.gradle.kts"))
                .map(f -> repo.read(f).orElse(""))
                .anyMatch(content -> content.contains(token));
    }

    public static boolean hasOpenApiSpec(RepoFiles repo) {
        return repo.allFiles().stream().anyMatch(f -> {
            String name = f.substring(f.lastIndexOf('/') + 1).toLowerCase();
            return name.equals("openapi.yml") || name.equals("openapi.yaml") || name.equals("openapi.json");
        });
    }
}
