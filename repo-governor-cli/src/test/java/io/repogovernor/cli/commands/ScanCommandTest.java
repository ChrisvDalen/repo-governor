package io.repogovernor.cli.commands;

import io.repogovernor.cli.RepoGovernorCli;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import picocli.CommandLine;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class ScanCommandTest {

    @TempDir
    Path repo;

    private int run(String... args) {
        return new CommandLine(new RepoGovernorCli()).execute(args);
    }

    @Test
    void scanWritesJsonAndMarkdownReports(@TempDir Path output) throws IOException {
        Files.writeString(repo.resolve("README.md"), "# Demo\nInstall, run and test instructions.");
        Files.writeString(repo.resolve(".gitignore"), "build/");

        int exitCode = run("scan", repo.toString(), "--output-dir", output.toString());

        assertThat(output.resolve(ScanCommand.JSON_REPORT)).exists();
        assertThat(output.resolve(ScanCommand.MARKDOWN_REPORT)).exists();
        String json = Files.readString(output.resolve(ScanCommand.JSON_REPORT));
        assertThat(json).contains("\"overallScore\"").contains("\"findings\"");
        assertThat(exitCode).isZero();
    }

    @Test
    void committedEnvFileViolatesBlockerThreshold() throws IOException {
        Files.writeString(repo.resolve("README.md"), "# Demo\nInstall, run and test instructions.");
        Files.writeString(repo.resolve(".env"), "DB_PASSWORD=hunter2");

        int exitCode = run("scan", repo.toString());

        // The default threshold config does not allow blocker findings.
        assertThat(exitCode).isEqualTo(2);
    }

    @Test
    void scanRespectsConfiguredRuleSelection() throws IOException {
        Files.writeString(repo.resolve("README.md"), "# Demo\nInstall, run and test instructions.");
        Files.writeString(repo.resolve("repo-governor.yml"), """
                project:
                  name: tiny
                  organization: demo-org
                rules:
                  enabled:
                    - repo.readme.exists
                thresholds:
                  overall: 0
                  blockerAllowed: true
                """);

        int exitCode = run("scan", repo.toString());

        assertThat(exitCode).isZero();
        String json = Files.readString(repo.resolve(ScanCommand.JSON_REPORT));
        assertThat(json).contains("\"repositoryName\" : \"tiny\"");
        assertThat(json).doesNotContain("repo.gitignore.exists");
    }

    @Test
    void initCreatesConfigFile() {
        int exitCode = run("init", repo.toString());

        assertThat(exitCode).isZero();
        assertThat(repo.resolve("repo-governor.yml")).exists();
    }
}
