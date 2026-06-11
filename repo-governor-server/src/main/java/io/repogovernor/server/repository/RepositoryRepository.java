package io.repogovernor.server.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RepositoryRepository extends JpaRepository<RepositoryEntity, UUID> {

    List<RepositoryEntity> findByOrganizationIdOrderByName(UUID organizationId);

    Optional<RepositoryEntity> findByOrganizationIdAndName(UUID organizationId, String name);

    Optional<RepositoryEntity> findByIdAndOrganizationId(UUID id, UUID organizationId);
}
