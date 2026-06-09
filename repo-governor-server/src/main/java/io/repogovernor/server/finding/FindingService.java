package io.repogovernor.server.finding;

import io.repogovernor.server.common.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
public class FindingService {

    private final FindingRepository findingRepository;

    public FindingService(FindingRepository findingRepository) {
        this.findingRepository = findingRepository;
    }

    public FindingDto updateStatus(UUID findingId, FindingStatus status) {
        FindingEntity finding = findingRepository.findById(findingId)
                .orElseThrow(() -> new NotFoundException("Finding", findingId));
        finding.setStatus(status);
        return FindingDto.from(findingRepository.save(finding));
    }
}
