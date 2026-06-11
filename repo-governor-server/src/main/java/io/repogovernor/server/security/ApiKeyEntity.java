package io.repogovernor.server.security;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "api_keys")
public class ApiKeyEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "organization_id", nullable = false)
    private UUID organizationId;

    @Column(name = "key_value", nullable = false, unique = true)
    private String keyValue;

    @Column(nullable = false)
    private String name;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    protected ApiKeyEntity() {
    }

    public ApiKeyEntity(UUID organizationId, String keyValue, String name) {
        this.organizationId = organizationId;
        this.keyValue = keyValue;
        this.name = name;
    }

    public UUID getId() {
        return id;
    }

    public UUID getOrganizationId() {
        return organizationId;
    }

    public String getKeyValue() {
        return keyValue;
    }

    public String getName() {
        return name;
    }
}
