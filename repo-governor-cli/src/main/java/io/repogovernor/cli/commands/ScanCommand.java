package io.repogovernor.cli.commands;

import io.repogovernor.cli.ToolVersion;
import io.repogovernor.cli.upload.ReportUploader;
import io.repogovernor.core.config.ConfigLoader;
import io.repogovernor.core.config.RepoGovernorConfig;
import io.repogovernor.core.domain.ScanReport;
import io.repogovernor.core.domain.Severity;
import io.repogovernor.core.reporting.JsonReportWriter;
import io.repogovernor.core.reporting.MarkdownReportWriter;
import io.repogovernor.core.rules.Rule;
import io.repogovernor.core.rules.Rules;
import io.repogovernor.core.scanner.DiskRepoFiles;
import io.repogovernor.core.scanner.RepoScanner;
import io.repogovernor.core.scanner.RepoScanner.ScanContext;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

@Command(name = "scan", description = "Scans a repository and writes repo-governor-report.json and .md.")
public final class ScanCommand implements Callable<Integer> {

    public static final String JSON_REPORT = "repo-governor-report.json";
    public static final String MARKDOWN_REPORT = "repo-governor-report.md";

    @Parameters(index = "0", defaultValue = ".", description = "Repository path (default: current directory).")
    private Path repositoryPath;

    @Option(names = "--upload", description = "Upload the report to the server after scanning.")
    private boolean upload;

    @Option(names = "--server", description = "Server base URL (overrides repo-governor.yml).")
    private String serverUrl;

    @Option(names = "--api-key", description = "API key for the server.")
    private String apiKey;

    @Option(names = "--output-dir", description = "Directory for the report files (default: repository root).")
    private Path outputDir;

    @Override
    public Integer call() throws Exception {
        var repo = new DiskRepoFiles(repositoryPath);
        RepoGovernorConfig config = new ConfigLoader().load(repo);

        List<Rule> rules = selectRules(config);
        var scanner = new RepoScanner(rules);
        ScanReport report = scanner.scan(repo, new ScanContext(
                config.project().name(),
                config.project().organization(),
                config.project().team(),
                ToolVersion.VERSION,
                Map.of()));

        Path targetDir = outputDir != null ? outputDir : repositoryPath;
        Files.createDirectories(targetDir);
        String json = new JsonReportWriter().write(report);
        Files.writeString(targetDir.resolve(JSON_REPORT), json);
        Files.writeString(targetDir.resolve(MARKDOWN_REPORT), new MarkdownReportWriter().write(report));

        printSummary(report, targetDir);

        if (upload) {
            String server = serverUrl != null ? serverUrl : config.server().url();
            if (apiKey == null) {
                System.err.println("Upload requested but no --api-key given.");
                return 1;
            }
            var result = new ReportUploader().upload(server, apiKey, json);
            if (result.isSuccess()) {
                System.out.println("Report uploaded to " + server);
            } else {
                System.err.println("Upload failed with HTTP " + result.statusCode() + ": " + result.body());
                return 1;
            }
        }

        return thresholdExitCode(report, config.thresholds());
    }

    private static List<Rule> selectRules(RepoGovernorConfig config) {
        List<String> enabled = config.rules().enabled();
        List<String> disabled = config.rules().disabled();
        return Rules.defaultRules().stream()
                .filter(rule -> enabled.isEmpty() || enabled.contains(rule.id()))
                .filter(rule -> !disabled.contains(rule.id()))
                .toList();
    }

    private static void printSummary(ScanReport report, Path targetDir) {
        System.out.println("Scanned " + report.repositoryName()
                + " — overall score " + report.overallScore() + "/100");
        report.categoryScores().forEach(score ->
                System.out.printf("  %-14s %3d%n", score.category(), score.score()));
        System.out.println("Findings: "
                + report.countBySeverity(Severity.BLOCKER) + " blocker, "
                + report.countBySeverity(Severity.MAJOR) + " major, "
                + report.countBySeverity(Severity.MINOR) + " minor, "
                + report.countBySeverity(Severity.INFO) + " info");
        System.out.println("Reports written to " + targetDir.toAbsolutePath());
    }

    private static int thresholdExitCode(ScanReport report, RepoGovernorConfig.Thresholds thresholds) {
        boolean blockerViolation = Boolean.FALSE.equals(thresholds.blockerAllowed())
                && report.countBySeverity(Severity.BLOCKER) > 0;
        boolean scoreViolation = thresholds.overall() != null
                && report.overallScore() < thresholds.overall();
        if (blockerViolation) {
            System.err.println("Threshold violated: blocker findings are not allowed.");
        }
        if (scoreViolation) {
            System.err.println("Threshold violated: overall score " + report.overallScore()
                    + " is below required " + thresholds.overall() + ".");
        }
        return (blockerViolation || scoreViolation) ? 2 : 0;
    }
}
