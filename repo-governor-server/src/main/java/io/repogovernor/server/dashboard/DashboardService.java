package io.repogovernor.server.dashboard;

import io.repogovernor.server.repository.RepositoryDtos.RepositorySummary;
import io.repogovernor.server.repository.RepositoryEntity;
import io.repogovernor.server.repository.RepositoryRepository;
import io.repogovernor.server.repository.RepositoryService;
import io.repogovernor.server.scan.ScanDtos.ScanSummary;
import io.repogovernor.server.scan.ScanEntity;
import io.repogovernor.server.scan.ScanQueryService;
import io.repogovernor.server.scan.ScanRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class DashboardService {

    private static final int TOP_LIST_SIZE = 5;

    private final RepositoryRepository repositoryRepository;
    private final RepositoryService repositoryService;
    private final ScanRepository scanRepository;
    private final ScanQueryService scanQueryService;

    public DashboardService(RepositoryRepository repositoryRepository,
                            RepositoryService repositoryService,
                            ScanRepository scanRepository,
                            ScanQueryService scanQueryService) {
        this.repositoryRepository = repositoryRepository;
        this.repositoryService = repositoryService;
        this.scanRepository = scanRepository;
        this.scanQueryService = scanQueryService;
    }

    public DashboardSummary summary(UUID organizationId) {
        List<RepositoryEntity> repositories =
                repositoryRepository.findByOrganizationIdOrderByName(organizationId);
        List<RepositorySummary> summaries = repositoryService.list(organizationId);

        List<RepositorySummary> scanned = summaries.stream()
                .filter(summary -> summary.latestScore() != null)
                .toList();
        int overallScore = (int) Math.round(scanned.stream()
                .mapToInt(RepositorySummary::latestScore)
                .average()
                .orElse(0));
        long blockerFindings = scanned.stream().mapToLong(RepositorySummary::blockerCount).sum();
        long majorFindings = scanned.stream().mapToLong(RepositorySummary::majorCount).sum();

        List<RepositorySummary> worst = scanned.stream()
                .sorted(Comparator.comparingInt(RepositorySummary::latestScore))
                .limit(TOP_LIST_SIZE)
                .toList();

        List<RepositoryTrend> improving = new ArrayList<>();
        List<RepositoryTrend> degrading = new ArrayList<>();
        for (RepositoryEntity repository : repositories) {
            trend(repository).ifPresent(trend -> {
                if (trend.currentScore() > trend.previousScore()) {
                    improving.add(trend);
                } else if (trend.currentScore() < trend.previousScore()) {
                    degrading.add(trend);
                }
            });
        }
        improving.sort(Comparator.comparingInt(t -> t.previousScore() - t.currentScore()));
        degrading.sort(Comparator.comparingInt(t -> t.currentScore() - t.previousScore()));

        List<ScanSummary> latestScans = repositories.stream()
                .map(repository -> scanRepository.findFirstByRepositoryIdOrderByScannedAtDesc(repository.getId()))
                .flatMap(java.util.Optional::stream)
                .sorted(Comparator.comparing(ScanEntity::getScannedAt).reversed())
                .limit(TOP_LIST_SIZE)
                .map(scan -> scanQueryService.listForRepository(scan.getRepositoryId()).stream()
                        .filter(summary -> summary.id().equals(scan.getId()))
                        .findFirst()
                        .orElseThrow())
                .toList();

        return new DashboardSummary(
                organizationId,
                overallScore,
                repositories.size(),
                blockerFindings,
                majorFindings,
                worst,
                List.copyOf(improving.subList(0, Math.min(TOP_LIST_SIZE, improving.size()))),
                List.copyOf(degrading.subList(0, Math.min(TOP_LIST_SIZE, degrading.size()))),
                latestScans);
    }

    private java.util.Optional<RepositoryTrend> trend(RepositoryEntity repository) {
        List<ScanEntity> scans = scanRepository.findByRepositoryIdOrderByScannedAtDesc(repository.getId());
        if (scans.size() < 2) {
            return java.util.Optional.empty();
        }
        return java.util.Optional.of(new RepositoryTrend(
                repository.getId(),
                repository.getName(),
                scans.get(0).getOverallScore(),
                scans.get(1).getOverallScore()));
    }

    public record RepositoryTrend(UUID repositoryId, String name, int currentScore, int previousScore) {
    }

    public record DashboardSummary(
            UUID organizationId,
            int overallScore,
            int repositoryCount,
            long blockerFindings,
            long majorFindings,
            List<RepositorySummary> worstRepositories,
            List<RepositoryTrend> improvingRepositories,
            List<RepositoryTrend> degradingRepositories,
            List<ScanSummary> latestScans) {
    }
}
