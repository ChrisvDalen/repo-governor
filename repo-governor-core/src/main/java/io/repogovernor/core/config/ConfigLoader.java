package io.repogovernor.core.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import io.repogovernor.core.scanner.RepoFiles;

import java.io.IOException;
import java.io.UncheckedIOException;

/**
 * Loads repo-governor.yml from a repository, falling back to defaults when the
 * file is absent.
 */
public final class ConfigLoader {

    public static final String CONFIG_FILE = "repo-governor.yml";

    private final ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory())
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    public RepoGovernorConfig load(RepoFiles repo) {
        return repo.read(CONFIG_FILE)
                .map(this::parse)
                .orElseGet(RepoGovernorConfig::defaults);
    }

    public RepoGovernorConfig parse(String yaml) {
        try {
            RepoGovernorConfig parsed = yamlMapper.readValue(yaml, RepoGovernorConfig.class);
            RepoGovernorConfig defaults = RepoGovernorConfig.defaults();
            return new RepoGovernorConfig(
                    parsed.project() != null ? parsed.project() : defaults.project(),
                    parsed.server() != null ? parsed.server() : defaults.server(),
                    parsed.rules() != null ? parsed.rules() : defaults.rules(),
                    parsed.thresholds() != null ? parsed.thresholds() : defaults.thresholds());
        } catch (IOException e) {
            throw new UncheckedIOException("Invalid " + CONFIG_FILE, e);
        }
    }
}
