package io.repogovernor.server.rule;

import io.repogovernor.server.common.Category;
import io.repogovernor.server.common.NotFoundException;
import io.repogovernor.server.common.Severity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional
public class RuleService {

    private final RuleRepository ruleRepository;
    private final RuleConfigurationRepository configurationRepository;

    public RuleService(RuleRepository ruleRepository, RuleConfigurationRepository configurationRepository) {
        this.ruleRepository = ruleRepository;
        this.configurationRepository = configurationRepository;
    }

    @Transactional(readOnly = true)
    public List<RuleWithConfiguration> list(UUID organizationId) {
        Map<UUID, RuleConfigurationEntity> configurations =
                configurationRepository.findByOrganizationId(organizationId).stream()
                        .collect(Collectors.toMap(RuleConfigurationEntity::getRuleId, Function.identity()));
        return ruleRepository.findAll().stream()
                .sorted((a, b) -> a.getRuleKey().compareTo(b.getRuleKey()))
                .map(rule -> RuleWithConfiguration.from(rule, configurations.get(rule.getId())))
                .toList();
    }

    public RuleWithConfiguration updateConfiguration(UUID organizationId, UUID ruleId,
                                                     Boolean enabled, Integer threshold) {
        RuleEntity rule = ruleRepository.findById(ruleId)
                .orElseThrow(() -> new NotFoundException("Rule", ruleId));
        RuleConfigurationEntity configuration = configurationRepository
                .findByRuleIdAndOrganizationId(ruleId, organizationId)
                .orElseGet(() -> new RuleConfigurationEntity(ruleId, organizationId));
        if (enabled != null) {
            configuration.setEnabled(enabled);
        }
        if (threshold != null) {
            configuration.setThreshold(threshold);
        }
        return RuleWithConfiguration.from(rule, configurationRepository.save(configuration));
    }

    public record RuleWithConfiguration(
            UUID id,
            String ruleKey,
            String title,
            String description,
            Category category,
            Severity severity,
            boolean enabled,
            Integer threshold) {

        static RuleWithConfiguration from(RuleEntity rule, RuleConfigurationEntity configuration) {
            return new RuleWithConfiguration(
                    rule.getId(),
                    rule.getRuleKey(),
                    rule.getTitle(),
                    rule.getDescription(),
                    rule.getCategory(),
                    rule.getSeverity(),
                    configuration == null || configuration.isEnabled(),
                    configuration == null ? null : configuration.getThreshold());
        }
    }
}
