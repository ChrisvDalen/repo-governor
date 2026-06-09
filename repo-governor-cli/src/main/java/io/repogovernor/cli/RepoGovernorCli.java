package io.repogovernor.cli;

import io.repogovernor.cli.commands.InitCommand;
import io.repogovernor.cli.commands.ReportCommand;
import io.repogovernor.cli.commands.RulesCommand;
import io.repogovernor.cli.commands.ScanCommand;
import io.repogovernor.cli.commands.UploadCommand;
import picocli.CommandLine;
import picocli.CommandLine.Command;

@Command(
        name = "repo-governor",
        description = "Scans repositories and scores them on engineering maturity.",
        mixinStandardHelpOptions = true,
        versionProvider = ToolVersion.class,
        subcommands = {
                InitCommand.class,
                ScanCommand.class,
                ReportCommand.class,
                UploadCommand.class,
                RulesCommand.class
        })
public final class RepoGovernorCli {

    public static void main(String[] args) {
        int exitCode = new CommandLine(new RepoGovernorCli()).execute(args);
        System.exit(exitCode);
    }
}
