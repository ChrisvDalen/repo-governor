package io.repogovernor.server.finding;

import io.repogovernor.server.common.Category;
import io.repogovernor.server.common.Severity;

import java.time.Instant;
import java.util.UUID;

public record FindingDto(
        UUID id,
        String ruleId,
        String title,
        String description,
        Severity severity,
        Category category,
        String filePath,
        Integer lineNumber,
        String recommendation,
        FindingStatus status,
        Instant createdAt) {

    public static FindingDto from(FindingEntity entity) {
        return new FindingDto(
                entity.getId(),
                entity.getRuleId(),
                entity.getTitle(),
                entity.getDescription(),
                entity.getSeverity(),
                entity.getCategory(),
                entity.getFilePath(),
                entity.getLineNumber(),
                entity.getRecommendation(),
                entity.getStatus(),
                entity.getCreatedAt());
    }
}
