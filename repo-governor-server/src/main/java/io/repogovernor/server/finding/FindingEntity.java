package io.repogovernor.server.finding;

import io.repogovernor.server.common.Category;
import io.repogovernor.server.common.Severity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "findings")
public class FindingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "scan_id", nullable = false)
    private UUID scanId;

    @Column(name = "repository_id", nullable = false)
    private UUID repositoryId;

    @Column(name = "rule_id", nullable = false)
    private String ruleId;

    @Column(nullable = false)
    private String title;

    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Severity severity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Category category;

    @Column(name = "file_path")
    private String filePath;

    @Column(name = "line_number")
    private Integer lineNumber;

    private String recommendation;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FindingStatus status = FindingStatus.OPEN;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    protected FindingEntity() {
    }

    public FindingEntity(UUID scanId, UUID repositoryId, String ruleId, String title, String description,
                         Severity severity, Category category, String filePath, Integer lineNumber,
                         String recommendation) {
        this.scanId = scanId;
        this.repositoryId = repositoryId;
        this.ruleId = ruleId;
        this.title = title;
        this.description = description;
        this.severity = severity;
        this.category = category;
        this.filePath = filePath;
        this.lineNumber = lineNumber;
        this.recommendation = recommendation;
    }

    public UUID getId() {
        return id;
    }

    public UUID getScanId() {
        return scanId;
    }

    public UUID getRepositoryId() {
        return repositoryId;
    }

    public String getRuleId() {
        return ruleId;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public Severity getSeverity() {
        return severity;
    }

    public Category getCategory() {
        return category;
    }

    public String getFilePath() {
        return filePath;
    }

    public Integer getLineNumber() {
        return lineNumber;
    }

    public String getRecommendation() {
        return recommendation;
    }

    public FindingStatus getStatus() {
        return status;
    }

    public void setStatus(FindingStatus status) {
        this.status = status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    /**
     * Identity of a finding across scans, used to carry over triage status.
     */
    public String matchKey() {
        return ruleId + "|" + (filePath == null ? "" : filePath);
    }
}
