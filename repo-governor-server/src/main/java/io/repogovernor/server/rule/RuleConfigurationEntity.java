package io.repogovernor.server.rule;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "rule_configurations")
public class RuleConfigurationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "rule_id", nullable = false)
    private UUID ruleId;

    @Column(name = "organization_id", nullable = false)
    private UUID organizationId;

    @Column(nullable = false)
    private boolean enabled = true;

    private Integer threshold;

    protected RuleConfigurationEntity() {
    }

    public RuleConfigurationEntity(UUID ruleId, UUID organizationId) {
        this.ruleId = ruleId;
        this.organizationId = organizationId;
    }

    public UUID getId() {
        return id;
    }

    public UUID getRuleId() {
        return ruleId;
    }

    public UUID getOrganizationId() {
        return organizationId;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public Integer getThreshold() {
        return threshold;
    }

    public void setThreshold(Integer threshold) {
        this.threshold = threshold;
    }
}
