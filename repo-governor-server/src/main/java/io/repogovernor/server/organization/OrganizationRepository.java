package io.repogovernor.server.organization;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface OrganizationRepository extends JpaRepository<OrganizationEntity, UUID> {

    Optional<OrganizationEntity> findByName(String name);
}
