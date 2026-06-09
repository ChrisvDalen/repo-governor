package io.repogovernor.server.scan;

import io.repogovernor.server.finding.FindingEntity;
import io.repogovernor.server.finding.FindingRepository;
import io.repogovernor.server.finding.FindingStatus;
import io.repogovernor.server.repository.RepositoryEntity;
import io.repogovernor.server.repository.RepositoryRepository;
import io.repogovernor.server.scan.ScanDtos.ScanReportPayload;
import io.repogovernor.server.scan.ScanDtos.ScanUploadResult;
import io.repogovernor.server.team.TeamService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Stores an uploaded CLI scan report: upserts the repository (and team),
 * persists the scan with its scores and findings, and carries over the triage
 * status of findings that already existed in the previous scan.
 */
@Service
@Transactional
public class ScanUploadService {

    private final RepositoryRepository repositoryRepository;
    private final ScanRepository scanRepository;
    private final FindingRepository findingRepository;
    private final TeamService teamService;

    public ScanUploadService(RepositoryRepository repositoryRepository,
                             ScanRepository scanRepository,
                             FindingRepository findingRepository,
                             TeamService teamService) {
        this.repositoryRepository = repositoryRepository;
        this.scanRepository = scanRepository;
        this.findingRepository = findingRepository;
        this.teamService = teamService;
    }

    public ScanUploadResult store(UUID organizationId, ScanReportPayload payload) {
        RepositoryEntity repository = findOrCreateRepository(organizationId, payload);

        Map<String, FindingStatus> previousStatuses = previousTriageStatuses(repository.getId());

        ScanEntity scan = scanRepository.save(new ScanEntity(
                repository.getId(),
                payload.branch(),
                payload.commitHash(),
                payload.scannedAt(),
                payload.toolVersion(),
                payload.overallScore(),
                payload.categoryScores().stream()
                        .map(score -> new ScanEntity.CategoryScoreEmbeddable(score.category(), score.score()))
                        .toList(),
                payload.detectedTechnologiesOrEmpty(),
                payload.metadataOrEmpty()));

        List<FindingEntity> findings = payload.findings().stream()
                .map(finding -> new FindingEntity(
                        scan.getId(),
                        repository.getId(),
                        finding.ruleId(),
                        finding.title(),
                        finding.description(),
                        finding.severity(),
                        finding.category(),
                        finding.filePath(),
                        finding.lineNumber(),
                        finding.recommendation()))
                .toList();
        findings.forEach(finding -> {
            FindingStatus carriedOver = previousStatuses.get(finding.matchKey());
            if (carriedOver != null) {
                finding.setStatus(carriedOver);
            }
        });
        findingRepository.saveAll(findings);

        return new ScanUploadResult(scan.getId(), repository.getId(), scan.getOverallScore());
    }

    private RepositoryEntity findOrCreateRepository(UUID organizationId, ScanReportPayload payload) {
        RepositoryEntity repository = repositoryRepository
                .findByOrganizationIdAndName(organizationId, payload.repositoryName())
                .orElseGet(() -> repositoryRepository.save(
                        new RepositoryEntity(organizationId, null, payload.repositoryName())));
        if (payload.team() != null && !payload.team().isBlank() && repository.getTeamId() == null) {
            repository.setTeamId(teamService.findOrCreate(organizationId, payload.team()).getId());
            repository = repositoryRepository.save(repository);
        }
        return repository;
    }

    private Map<String, FindingStatus> previousTriageStatuses(UUID repositoryId) {
        return scanRepository.findFirstByRepositoryIdOrderByScannedAtDesc(repositoryId)
                .map(previousScan -> findingRepository.findByScanId(previousScan.getId()).stream()
                        .filter(finding -> finding.getStatus() != FindingStatus.OPEN)
                        .collect(Collectors.toMap(
                                FindingEntity::matchKey,
                                FindingEntity::getStatus,
                                (first, second) -> first)))
                .orElse(Map.of());
    }
}
