package io.repogovernor.core.config;

import java.util.List;

/**
 * Parsed representation of repo-governor.yml.
 */
public record RepoGovernorConfig(
        Project project,
        Server server,
        RulesConfig rules,
        Thresholds thresholds) {

    public record Project(String name, String type, String organization, String team) {
    }

    public record Server(String url) {
    }

    public record RulesConfig(List<String> enabled, List<String> disabled) {
        public RulesConfig {
            enabled = enabled == null ? List.of() : List.copyOf(enabled);
            disabled = disabled == null ? List.of() : List.copyOf(disabled);
        }
    }

    public record Thresholds(Integer overall, Boolean blockerAllowed) {
    }

    public static RepoGovernorConfig defaults() {
        return new RepoGovernorConfig(
                new Project(null, "auto", "demo-org", null),
                new Server("http://localhost:8080"),
                new RulesConfig(List.of(), List.of()),
                new Thresholds(70, false));
    }
}
