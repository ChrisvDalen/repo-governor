package io.repogovernor.server.rule;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RuleConfigurationRepository extends JpaRepository<RuleConfigurationEntity, UUID> {

    List<RuleConfigurationEntity> findByOrganizationId(UUID organizationId);

    Optional<RuleConfigurationEntity> findByRuleIdAndOrganizationId(UUID ruleId, UUID organizationId);
}
