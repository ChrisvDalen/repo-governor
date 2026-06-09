package io.repogovernor.core.reporting;

import io.repogovernor.core.domain.CategoryScore;
import io.repogovernor.core.domain.Finding;
import io.repogovernor.core.domain.ScanReport;
import io.repogovernor.core.domain.Severity;

import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

/**
 * Renders a {@link ScanReport} as a human-readable Markdown document.
 */
public final class MarkdownReportWriter {

    private static final DateTimeFormatter TIMESTAMP =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm 'UTC'").withZone(ZoneOffset.UTC);

    public String write(ScanReport report) {
        StringBuilder md = new StringBuilder();
        md.append("# Repo Governor Report — ").append(report.repositoryName()).append("\n\n");
        md.append("| | |\n|---|---|\n");
        md.append("| **Overall score** | ").append(report.overallScore()).append(" / 100 |\n");
        if (report.branch() != null) {
            md.append("| Branch | ").append(report.branch()).append(" |\n");
        }
        if (report.commitHash() != null) {
            md.append("| Commit | `").append(report.commitHash()).append("` |\n");
        }
        md.append("| Scanned at | ").append(TIMESTAMP.format(report.scannedAt())).append(" |\n");
        md.append("| Tool version | ").append(report.toolVersion()).append(" |\n");
        md.append("| Technologies | ").append(String.join(", ", report.detectedTechnologies())).append(" |\n\n");

        md.append("## Category scores\n\n");
        md.append("| Category | Score |\n|---|---|\n");
        for (CategoryScore score : report.categoryScores()) {
            md.append("| ").append(score.category()).append(" | ").append(score.score()).append(" |\n");
        }
        md.append('\n');

        md.append("## Findings (").append(report.findings().size()).append(")\n\n");
        if (report.findings().isEmpty()) {
            md.append("No findings. \n");
        } else {
            for (Severity severity : Severity.values()) {
                var findings = report.findings().stream()
                        .filter(f -> f.severity() == severity)
                        .toList();
                if (findings.isEmpty()) {
                    continue;
                }
                md.append("### ").append(severity).append(" (").append(findings.size()).append(")\n\n");
                for (Finding finding : findings) {
                    md.append("- **").append(finding.title()).append("** (`").append(finding.ruleId()).append("`, ")
                            .append(finding.category()).append(")\n");
                    if (finding.description() != null) {
                        md.append("  - ").append(finding.description()).append('\n');
                    }
                    if (finding.filePath() != null) {
                        md.append("  - File: `").append(finding.filePath());
                        if (finding.lineNumber() != null) {
                            md.append(':').append(finding.lineNumber());
                        }
                        md.append("`\n");
                    }
                    if (finding.recommendation() != null) {
                        md.append("  - Recommendation: ").append(finding.recommendation()).append('\n');
                    }
                }
                md.append('\n');
            }
        }
        return md.toString();
    }
}
