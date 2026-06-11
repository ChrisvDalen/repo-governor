package io.repogovernor.server.scan;

import io.repogovernor.server.common.NotFoundException;
import io.repogovernor.server.finding.FindingDto;
import io.repogovernor.server.finding.FindingEntity;
import io.repogovernor.server.finding.FindingRepository;
import io.repogovernor.server.scan.ScanDtos.ScanDetail;
import io.repogovernor.server.scan.ScanDtos.ScanDiff;
import io.repogovernor.server.scan.ScanDtos.ScanSummary;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class ScanQueryService {

    private final ScanRepository scanRepository;
    private final FindingRepository findingRepository;

    public ScanQueryService(ScanRepository scanRepository, FindingRepository findingRepository) {
        this.scanRepository = scanRepository;
        this.findingRepository = findingRepository;
    }

    public List<ScanSummary> listForRepository(UUID repositoryId) {
        return scanRepository.findByRepositoryIdOrderByScannedAtDesc(repositoryId).stream()
                .map(scan -> ScanSummary.from(scan, findingRepository.findByScanId(scan.getId())))
                .toList();
    }

    public ScanDetail get(UUID scanId) {
        ScanEntity scan = scanRepository.findById(scanId)
                .orElseThrow(() -> new NotFoundException("Scan", scanId));
        return ScanDetail.from(scan, findingRepository.findByScanId(scanId));
    }

    /**
     * Compares a scan with the previous scan of the same repository.
     */
    public ScanDiff diff(UUID scanId) {
        ScanEntity scan = scanRepository.findById(scanId)
                .orElseThrow(() -> new NotFoundException("Scan", scanId));
        List<FindingEntity> current = findingRepository.findByScanId(scanId);

        var previousScan = scanRepository
                .findFirstByRepositoryIdAndScannedAtBeforeOrderByScannedAtDesc(
                        scan.getRepositoryId(), scan.getScannedAt());
        if (previousScan.isEmpty()) {
            return new ScanDiff(scanId, null, 0,
                    current.stream().map(FindingDto::from).toList(), List.of());
        }

        List<FindingEntity> previous = findingRepository.findByScanId(previousScan.get().getId());
        Set<String> currentKeys = keys(current);
        Set<String> previousKeys = keys(previous);

        List<FindingDto> newFindings = current.stream()
                .filter(f -> !previousKeys.contains(f.matchKey()))
                .map(FindingDto::from)
                .toList();
        List<FindingDto> resolvedFindings = previous.stream()
                .filter(f -> !currentKeys.contains(f.matchKey()))
                .map(FindingDto::from)
                .toList();

        return new ScanDiff(
                scanId,
                previousScan.get().getId(),
                scan.getOverallScore() - previousScan.get().getOverallScore(),
                newFindings,
                resolvedFindings);
    }

    private static Set<String> keys(List<FindingEntity> findings) {
        return findings.stream().map(FindingEntity::matchKey).collect(Collectors.toSet());
    }
}
