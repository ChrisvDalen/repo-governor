package io.repogovernor.server.scan;

import io.repogovernor.server.common.Category;
import io.repogovernor.server.common.Severity;
import io.repogovernor.server.finding.FindingDto;
import io.repogovernor.server.finding.FindingEntity;
import io.repogovernor.server.finding.FindingStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * DTOs at the scan API boundary. {@link ScanReportPayload} mirrors the JSON
 * report produced by the CLI (see repo-governor-contracts/openapi.yaml).
 */
public final class ScanDtos {

    private ScanDtos() {
    }

    public record CategoryScoreDto(Category category, int score) {

        static CategoryScoreDto from(ScanEntity.CategoryScoreEmbeddable embeddable) {
            return new CategoryScoreDto(embeddable.getCategory(), embeddable.getScore());
        }
    }

    public record ReportFinding(
            @NotBlank String ruleId,
            @NotBlank String title,
            String description,
            @NotNull Severity severity,
            @NotNull Category category,
            String filePath,
            Integer lineNumber,
            String recommendation) {
    }

    public record ScanReportPayload(
            @NotBlank String repositoryName,
            String organization,
            String team,
            String branch,
            String commitHash,
            @NotNull Instant scannedAt,
            @NotBlank String toolVersion,
            int overallScore,
            @NotNull List<CategoryScoreDto> categoryScores,
            @NotNull List<ReportFinding> findings,
            List<String> detectedTechnologies,
            Map<String, String> metadata) {

        public List<String> detectedTechnologiesOrEmpty() {
            return detectedTechnologies == null ? List.of() : detectedTechnologies;
        }

        public Map<String, String> metadataOrEmpty() {
            return metadata == null ? Map.of() : metadata;
        }
    }

    public record ScanUploadResult(UUID scanId, UUID repositoryId, int overallScore) {
    }

    public record ScanSummary(
            UUID id,
            UUID repositoryId,
            String branch,
            String commitHash,
            Instant scannedAt,
            String toolVersion,
            int overallScore,
            long blockerCount,
            long majorCount) {

        static ScanSummary from(ScanEntity scan, List<FindingEntity> findings) {
            return new ScanSummary(
                    scan.getId(),
                    scan.getRepositoryId(),
                    scan.getBranch(),
                    scan.getCommitHash(),
                    scan.getScannedAt(),
                    scan.getToolVersion(),
                    scan.getOverallScore(),
                    countOpen(findings, Severity.BLOCKER),
                    countOpen(findings, Severity.MAJOR));
        }

        private static long countOpen(List<FindingEntity> findings, Severity severity) {
            return findings.stream()
                    .filter(f -> f.getSeverity() == severity && f.getStatus() == FindingStatus.OPEN)
                    .count();
        }
    }

    public record ScanDetail(
            UUID id,
            UUID repositoryId,
            String branch,
            String commitHash,
            Instant scannedAt,
            String toolVersion,
            int overallScore,
            long blockerCount,
            long majorCount,
            List<CategoryScoreDto> categoryScores,
            List<FindingDto> findings,
            List<String> detectedTechnologies,
            Map<String, String> metadata) {

        static ScanDetail from(ScanEntity scan, List<FindingEntity> findings) {
            ScanSummary summary = ScanSummary.from(scan, findings);
            return new ScanDetail(
                    summary.id(),
                    summary.repositoryId(),
                    summary.branch(),
                    summary.commitHash(),
                    summary.scannedAt(),
                    summary.toolVersion(),
                    summary.overallScore(),
                    summary.blockerCount(),
                    summary.majorCount(),
                    scan.getCategoryScores().stream().map(CategoryScoreDto::from).toList(),
                    findings.stream().map(FindingDto::from).toList(),
                    List.copyOf(scan.getTechnologies()),
                    Map.copyOf(scan.getMetadata()));
        }
    }

    public record ScanDiff(
            UUID scanId,
            UUID previousScanId,
            int scoreDelta,
            List<FindingDto> newFindings,
            List<FindingDto> resolvedFindings) {
    }
}
