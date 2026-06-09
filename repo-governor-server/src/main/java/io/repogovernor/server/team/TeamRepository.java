package io.repogovernor.server.team;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TeamRepository extends JpaRepository<TeamEntity, UUID> {

    List<TeamEntity> findByOrganizationId(UUID organizationId);

    Optional<TeamEntity> findByOrganizationIdAndName(UUID organizationId, String name);
}
