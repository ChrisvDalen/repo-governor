package io.repogovernor.server.scan;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ScanRepository extends JpaRepository<ScanEntity, UUID> {

    List<ScanEntity> findByRepositoryIdOrderByScannedAtDesc(UUID repositoryId);

    Optional<ScanEntity> findFirstByRepositoryIdOrderByScannedAtDesc(UUID repositoryId);

    Optional<ScanEntity> findFirstByRepositoryIdAndScannedAtBeforeOrderByScannedAtDesc(
            UUID repositoryId, Instant scannedAt);
}
