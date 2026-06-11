package io.repogovernor.core.config;

import io.repogovernor.core.testsupport.InMemoryRepoFiles;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ConfigLoaderTest {

    private final ConfigLoader loader = new ConfigLoader();

    @Test
    void parsesFullConfig() {
        var config = loader.parse("""
                project:
                  name: example-service
                  type: auto
                  organization: demo-org
                  team: platform
                server:
                  url: http://localhost:8080
                rules:
                  enabled:
                    - repo.readme.exists
                    - java.junit.exists
                thresholds:
                  overall: 70
                  blockerAllowed: false
                """);

        assertThat(config.project().name()).isEqualTo("example-service");
        assertThat(config.project().team()).isEqualTo("platform");
        assertThat(config.server().url()).isEqualTo("http://localhost:8080");
        assertThat(config.rules().enabled()).containsExactly("repo.readme.exists", "java.junit.exists");
        assertThat(config.thresholds().overall()).isEqualTo(70);
        assertThat(config.thresholds().blockerAllowed()).isFalse();
    }

    @Test
    void missingFileFallsBackToDefaults() {
        var config = loader.load(InMemoryRepoFiles.repo());

        assertThat(config.project().organization()).isEqualTo("demo-org");
        assertThat(config.server().url()).isEqualTo("http://localhost:8080");
        assertThat(config.rules().enabled()).isEmpty();
    }

    @Test
    void partialConfigKeepsDefaultsForMissingSections() {
        var config = loader.parse("""
                project:
                  name: only-name
                """);

        assertThat(config.project().name()).isEqualTo("only-name");
        assertThat(config.server().url()).isEqualTo("http://localhost:8080");
        assertThat(config.thresholds().overall()).isEqualTo(70);
    }
}
