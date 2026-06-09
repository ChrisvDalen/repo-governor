package io.repogovernor.server.organization;

import java.time.Instant;
import java.util.UUID;

public record OrganizationDto(UUID id, String name, Instant createdAt) {

    static OrganizationDto from(OrganizationEntity entity) {
        return new OrganizationDto(entity.getId(), entity.getName(), entity.getCreatedAt());
    }
}
