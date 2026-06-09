package io.repogovernor.core.domain;

import java.util.Objects;

/**
 * A single rule violation or observation produced by a scan.
 */
public record Finding(
        String ruleId,
        String title,
        String description,
        Severity severity,
        Category category,
        String filePath,
        Integer lineNumber,
        String recommendation) {

    public Finding {
        Objects.requireNonNull(ruleId, "ruleId");
        Objects.requireNonNull(title, "title");
        Objects.requireNonNull(severity, "severity");
        Objects.requireNonNull(category, "category");
    }

    public static Builder builder(String ruleId) {
        return new Builder(ruleId);
    }

    public static final class Builder {
        private final String ruleId;
        private String title;
        private String description;
        private Severity severity = Severity.MINOR;
        private Category category = Category.REPO_HEALTH;
        private String filePath;
        private Integer lineNumber;
        private String recommendation;

        private Builder(String ruleId) {
            this.ruleId = ruleId;
        }

        public Builder title(String title) {
            this.title = title;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder severity(Severity severity) {
            this.severity = severity;
            return this;
        }

        public Builder category(Category category) {
            this.category = category;
            return this;
        }

        public Builder filePath(String filePath) {
            this.filePath = filePath;
            return this;
        }

        public Builder lineNumber(Integer lineNumber) {
            this.lineNumber = lineNumber;
            return this;
        }

        public Builder recommendation(String recommendation) {
            this.recommendation = recommendation;
            return this;
        }

        public Finding build() {
            return new Finding(ruleId, title, description, severity, category, filePath, lineNumber, recommendation);
        }
    }
}
