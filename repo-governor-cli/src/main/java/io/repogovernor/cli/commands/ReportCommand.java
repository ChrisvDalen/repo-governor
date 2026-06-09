package io.repogovernor.cli.commands;

import io.repogovernor.core.domain.ScanReport;
import io.repogovernor.core.reporting.JsonReportWriter;
import io.repogovernor.core.reporting.MarkdownReportWriter;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.Callable;

@Command(name = "report", description = "Prints the report from an earlier scan.")
public final class ReportCommand implements Callable<Integer> {

    @Parameters(index = "0", defaultValue = ".",
            description = "Repository path containing " + ScanCommand.JSON_REPORT + ".")
    private Path repositoryPath;

    @Option(names = "--format", defaultValue = "markdown", description = "Output format: markdown or json.")
    private String format;

    @Override
    public Integer call() throws Exception {
        Path jsonFile = repositoryPath.resolve(ScanCommand.JSON_REPORT);
        if (!Files.exists(jsonFile)) {
            System.err.println("No " + ScanCommand.JSON_REPORT + " found in "
                    + repositoryPath.toAbsolutePath() + ". Run 'repo-governor scan' first.");
            return 1;
        }
        String json = Files.readString(jsonFile);
        if ("json".equalsIgnoreCase(format)) {
            System.out.println(json);
        } else {
            ScanReport report = new JsonReportWriter().read(json);
            System.out.println(new MarkdownReportWriter().write(report));
        }
        return 0;
    }
}
