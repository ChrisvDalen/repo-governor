package io.repogovernor.server.rule;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface RuleRepository extends JpaRepository<RuleEntity, UUID> {
}
