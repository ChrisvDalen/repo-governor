package io.repogovernor.server.repository;

import io.repogovernor.server.common.Category;
import io.repogovernor.server.common.NotFoundException;
import io.repogovernor.server.common.Severity;
import io.repogovernor.server.finding.FindingDto;
import io.repogovernor.server.finding.FindingEntity;
import io.repogovernor.server.finding.FindingRepository;
import io.repogovernor.server.finding.FindingStatus;
import io.repogovernor.server.repository.RepositoryDtos.RegisterRepositoryRequest;
import io.repogovernor.server.repository.RepositoryDtos.RepositoryDetail;
import io.repogovernor.server.repository.RepositoryDtos.RepositorySummary;
import io.repogovernor.server.scan.ScanEntity;
import io.repogovernor.server.scan.ScanQueryService;
import io.repogovernor.server.scan.ScanRepository;
import io.repogovernor.server.team.TeamEntity;
import io.repogovernor.server.team.TeamRepository;
import io.repogovernor.server.team.TeamService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class RepositoryService {

    private final RepositoryRepository repositoryRepository;
    private final ScanRepository scanRepository;
    private final ScanQueryService scanQueryService;
    private final FindingRepository findingRepository;
    private final TeamRepository teamRepository;
    private final TeamService teamService;

    public RepositoryService(RepositoryRepository repositoryRepository,
                             ScanRepository scanRepository,
                             ScanQueryService scanQueryService,
                             FindingRepository findingRepository,
                             TeamRepository teamRepository,
                             TeamService teamService) {
        this.repositoryRepository = repositoryRepository;
        this.scanRepository = scanRepository;
        this.scanQueryService = scanQueryService;
        this.findingRepository = findingRepository;
        this.teamRepository = teamRepository;
        this.teamService = teamService;
    }

    @Transactional(readOnly = true)
    public List<RepositorySummary> list(UUID organizationId) {
        return repositoryRepository.findByOrganizationIdOrderByName(organizationId).stream()
                .map(this::toSummary)
                .toList();
    }

    @Transactional(readOnly = true)
    public RepositoryDetail get(UUID organizationId, UUID repositoryId) {
        RepositoryEntity repository = requireRepository(organizationId, repositoryId);
        RepositorySummary summary = toSummary(repository);
        var latestScan = scanRepository.findFirstByRepositoryIdOrderByScannedAtDesc(repositoryId)
                .map(scan -> scanQueryService.get(scan.getId()))
                .orElse(null);
        return new RepositoryDetail(
                summary.id(), summary.name(), summary.teamName(), summary.latestScore(),
                summary.blockerCount(), summary.majorCount(), summary.lastScannedAt(),
                summary.detectedTechnologies(), latestScan);
    }

    /**
     * Findings of the latest scan, optionally filtered.
     */
    @Transactional(readOnly = true)
    public List<FindingDto> findings(UUID organizationId, UUID repositoryId,
                                     Severity severity, Category category, FindingStatus status) {
        requireRepository(organizationId, repositoryId);
        return scanRepository.findFirstByRepositoryIdOrderByScannedAtDesc(repositoryId)
                .map(scan -> findingRepository.findByScanId(scan.getId()).stream()
                        .filter(f -> severity == null || f.getSeverity() == severity)
                        .filter(f -> category == null || f.getCategory() == category)
                        .filter(f -> status == null || f.getStatus() == status)
                        .map(FindingDto::from)
                        .toList())
                .orElse(List.of());
    }

    public RepositorySummary register(UUID organizationId, RegisterRepositoryRequest request) {
        repositoryRepository.findByOrganizationIdAndName(organizationId, request.name())
                .ifPresent(existing -> {
                    throw new IllegalArgumentException("Repository '" + request.name() + "' already exists");
                });
        UUID teamId = null;
        if (request.teamName() != null && !request.teamName().isBlank()) {
            teamId = teamService.findOrCreate(organizationId, request.teamName()).getId();
        }
        return toSummary(repositoryRepository.save(
                new RepositoryEntity(organizationId, teamId, request.name())));
    }

    private RepositoryEntity requireRepository(UUID organizationId, UUID repositoryId) {
        return repositoryRepository.findByIdAndOrganizationId(repositoryId, organizationId)
                .orElseThrow(() -> new NotFoundException("Repository", repositoryId));
    }

    private RepositorySummary toSummary(RepositoryEntity repository) {
        Optional<ScanEntity> latestScan =
                scanRepository.findFirstByRepositoryIdOrderByScannedAtDesc(repository.getId());
        String teamName = Optional.ofNullable(repository.getTeamId())
                .flatMap(teamRepository::findById)
                .map(TeamEntity::getName)
                .orElse(null);
        return new RepositorySummary(
                repository.getId(),
                repository.getName(),
                teamName,
                latestScan.map(ScanEntity::getOverallScore).orElse(null),
                latestScan.map(scan -> countOpen(scan, Severity.BLOCKER)).orElse(0L),
                latestScan.map(scan -> countOpen(scan, Severity.MAJOR)).orElse(0L),
                latestScan.map(ScanEntity::getScannedAt).orElse(null),
                latestScan.map(scan -> List.copyOf(scan.getTechnologies())).orElse(List.of()));
    }

    private long countOpen(ScanEntity scan, Severity severity) {
        return findingRepository.countByScanIdAndSeverityAndStatus(scan.getId(), severity, FindingStatus.OPEN);
    }
}
