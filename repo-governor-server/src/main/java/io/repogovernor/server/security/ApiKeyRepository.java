package io.repogovernor.server.security;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ApiKeyRepository extends JpaRepository<ApiKeyEntity, UUID> {

    Optional<ApiKeyEntity> findByKeyValue(String keyValue);
}
