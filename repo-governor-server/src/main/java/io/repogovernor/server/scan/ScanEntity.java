package io.repogovernor.server.scan;

import io.repogovernor.server.common.Category;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapKeyColumn;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "scans")
public class ScanEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "repository_id", nullable = false)
    private UUID repositoryId;

    private String branch;

    @Column(name = "commit_hash")
    private String commitHash;

    @Column(name = "scanned_at", nullable = false)
    private Instant scannedAt;

    @Column(name = "tool_version", nullable = false)
    private String toolVersion;

    @Column(name = "overall_score", nullable = false)
    private int overallScore;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "scan_category_scores", joinColumns = @JoinColumn(name = "scan_id"))
    private List<CategoryScoreEmbeddable> categoryScores = new ArrayList<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "scan_technologies", joinColumns = @JoinColumn(name = "scan_id"))
    @Column(name = "technology")
    @OrderColumn(name = "position")
    private List<String> technologies = new ArrayList<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "scan_metadata", joinColumns = @JoinColumn(name = "scan_id"))
    @MapKeyColumn(name = "meta_key")
    @Column(name = "meta_value")
    private Map<String, String> metadata = new LinkedHashMap<>();

    protected ScanEntity() {
    }

    public ScanEntity(UUID repositoryId, String branch, String commitHash, Instant scannedAt,
                      String toolVersion, int overallScore,
                      List<CategoryScoreEmbeddable> categoryScores,
                      List<String> technologies,
                      Map<String, String> metadata) {
        this.repositoryId = repositoryId;
        this.branch = branch;
        this.commitHash = commitHash;
        this.scannedAt = scannedAt;
        this.toolVersion = toolVersion;
        this.overallScore = overallScore;
        this.categoryScores = new ArrayList<>(categoryScores);
        this.technologies = new ArrayList<>(technologies);
        this.metadata = new LinkedHashMap<>(metadata);
    }

    public UUID getId() {
        return id;
    }

    public UUID getRepositoryId() {
        return repositoryId;
    }

    public String getBranch() {
        return branch;
    }

    public String getCommitHash() {
        return commitHash;
    }

    public Instant getScannedAt() {
        return scannedAt;
    }

    public String getToolVersion() {
        return toolVersion;
    }

    public int getOverallScore() {
        return overallScore;
    }

    public List<CategoryScoreEmbeddable> getCategoryScores() {
        return categoryScores;
    }

    public List<String> getTechnologies() {
        return technologies;
    }

    public Map<String, String> getMetadata() {
        return metadata;
    }

    @Embeddable
    public static class CategoryScoreEmbeddable {

        @Enumerated(EnumType.STRING)
        @Column(nullable = false)
        private Category category;

        @Column(nullable = false)
        private int score;

        protected CategoryScoreEmbeddable() {
        }

        public CategoryScoreEmbeddable(Category category, int score) {
            this.category = category;
            this.score = score;
        }

        public Category getCategory() {
            return category;
        }

        public int getScore() {
            return score;
        }
    }
}
