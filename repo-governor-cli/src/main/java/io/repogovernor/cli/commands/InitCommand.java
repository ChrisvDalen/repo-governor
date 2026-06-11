package io.repogovernor.cli.commands;

import io.repogovernor.core.config.ConfigLoader;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.Callable;

@Command(name = "init", description = "Generates a repo-governor.yml in the target repository.")
public final class InitCommand implements Callable<Integer> {

    @Parameters(index = "0", defaultValue = ".", description = "Repository path (default: current directory).")
    private Path repositoryPath;

    @Override
    public Integer call() throws Exception {
        Path configFile = repositoryPath.resolve(ConfigLoader.CONFIG_FILE);
        if (Files.exists(configFile)) {
            System.out.println(ConfigLoader.CONFIG_FILE + " already exists at " + configFile.toAbsolutePath());
            return 1;
        }
        String projectName = repositoryPath.toAbsolutePath().normalize().getFileName().toString();
        Files.writeString(configFile, """
                project:
                  name: %s
                  type: auto
                  organization: demo-org
                  team: platform
                server:
                  url: http://localhost:8080
                rules:
                  enabled: []   # empty means: all default rules
                thresholds:
                  overall: 70
                  blockerAllowed: false
                """.formatted(projectName));
        System.out.println("Created " + configFile.toAbsolutePath());
        return 0;
    }
}
