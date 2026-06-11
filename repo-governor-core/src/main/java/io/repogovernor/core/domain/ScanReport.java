package io.repogovernor.core.domain;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * The complete result of scanning one repository. This is the payload the CLI
 * writes to disk and uploads to the server.
 */
public record ScanReport(
        String repositoryName,
        String organization,
        String team,
        String branch,
        String commitHash,
        Instant scannedAt,
        String toolVersion,
        int overallScore,
        List<CategoryScore> categoryScores,
        List<Finding> findings,
        List<String> detectedTechnologies,
        Map<String, String> metadata) {

    public ScanReport {
        categoryScores = List.copyOf(categoryScores);
        findings = List.copyOf(findings);
        detectedTechnologies = List.copyOf(detectedTechnologies);
        metadata = Map.copyOf(metadata);
    }

    public long countBySeverity(Severity severity) {
        return findings.stream().filter(f -> f.severity() == severity).count();
    }
}
