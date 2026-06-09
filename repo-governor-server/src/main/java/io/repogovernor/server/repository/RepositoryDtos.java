package io.repogovernor.server.repository;

import io.repogovernor.server.scan.ScanDtos.ScanDetail;
import jakarta.validation.constraints.NotBlank;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public final class RepositoryDtos {

    private RepositoryDtos() {
    }

    public record RegisterRepositoryRequest(@NotBlank String name, String teamName) {
    }

    public record RepositorySummary(
            UUID id,
            String name,
            String teamName,
            Integer latestScore,
            long blockerCount,
            long majorCount,
            Instant lastScannedAt,
            List<String> detectedTechnologies) {
    }

    public record RepositoryDetail(
            UUID id,
            String name,
            String teamName,
            Integer latestScore,
            long blockerCount,
            long majorCount,
            Instant lastScannedAt,
            List<String> detectedTechnologies,
            ScanDetail latestScan) {
    }
}
