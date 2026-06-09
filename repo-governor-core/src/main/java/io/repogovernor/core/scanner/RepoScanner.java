package io.repogovernor.core.scanner;

import io.repogovernor.core.domain.Category;
import io.repogovernor.core.domain.CategoryScore;
import io.repogovernor.core.domain.Finding;
import io.repogovernor.core.domain.ScanReport;
import io.repogovernor.core.rules.Rule;
import io.repogovernor.core.scoring.ScoreCalculator;

import java.time.Instant;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Orchestrates a scan: detects technologies, runs all applicable rules,
 * calculates scores and assembles the {@link ScanReport}. The scanner never
 * writes to the console or to disk; reporting is a separate concern.
 */
public final class RepoScanner {

    private final List<Rule> rules;
    private final TechnologyDetector technologyDetector = new TechnologyDetector();
    private final ScoreCalculator scoreCalculator = new ScoreCalculator();

    public RepoScanner(List<Rule> rules) {
        this.rules = List.copyOf(rules);
    }

    public ScanReport scan(RepoFiles repo, ScanContext context) {
        Set<String> technologies = technologyDetector.detect(repo);

        List<Finding> findings = new ArrayList<>();
        Set<Category> relevantCategories = EnumSet.noneOf(Category.class);
        for (Rule rule : rules) {
            if (!rule.appliesTo(repo)) {
                continue;
            }
            relevantCategories.add(rule.category());
            findings.addAll(rule.evaluate(repo));
        }

        List<CategoryScore> categoryScores = scoreCalculator.categoryScores(relevantCategories, findings);
        int overallScore = scoreCalculator.overallScore(categoryScores);

        GitInfo gitInfo = GitInfo.from(repo);
        Map<String, String> metadata = new LinkedHashMap<>(context.metadata());
        metadata.putIfAbsent("rulesEvaluated", String.valueOf(rules.size()));

        return new ScanReport(
                context.repositoryName() != null ? context.repositoryName() : repo.repositoryName(),
                context.organization(),
                context.team(),
                gitInfo.branch().orElse(null),
                gitInfo.commitHash().orElse(null),
                Instant.now(),
                context.toolVersion(),
                overallScore,
                categoryScores,
                findings,
                List.copyOf(technologies),
                metadata);
    }

    /**
     * Caller-supplied context for a scan (project identity and tool version).
     */
    public record ScanContext(
            String repositoryName,
            String organization,
            String team,
            String toolVersion,
            Map<String, String> metadata) {

        public ScanContext {
            metadata = Map.copyOf(metadata);
        }

        public static ScanContext of(String organization, String team, String toolVersion) {
            return new ScanContext(null, organization, team, toolVersion, Map.of());
        }
    }
}
