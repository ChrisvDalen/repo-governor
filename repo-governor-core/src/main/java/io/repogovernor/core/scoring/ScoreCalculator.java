package io.repogovernor.core.scoring;

import io.repogovernor.core.domain.Category;
import io.repogovernor.core.domain.CategoryScore;
import io.repogovernor.core.domain.Finding;
import io.repogovernor.core.domain.Severity;

import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Calculates category scores (0-100) from findings. Each relevant category
 * starts at 100 and loses points per finding, weighted by severity. Categories
 * that are not relevant for the repository are not scored at all.
 */
public final class ScoreCalculator {

    private static final Map<Severity, Integer> PENALTIES = Map.of(
            Severity.BLOCKER, 45,
            Severity.MAJOR, 15,
            Severity.MINOR, 5,
            Severity.INFO, 1);

    public List<CategoryScore> categoryScores(Set<Category> relevantCategories, List<Finding> findings) {
        Map<Category, Integer> penalties = new EnumMap<>(Category.class);
        for (Finding finding : findings) {
            penalties.merge(finding.category(), PENALTIES.get(finding.severity()), Integer::sum);
        }
        return relevantCategories.stream()
                .sorted(Comparator.comparing(Enum::ordinal))
                .map(category -> new CategoryScore(category,
                        Math.max(0, 100 - penalties.getOrDefault(category, 0))))
                .toList();
    }

    public int overallScore(List<CategoryScore> categoryScores) {
        if (categoryScores.isEmpty()) {
            return 100;
        }
        double average = categoryScores.stream().mapToInt(CategoryScore::score).average().orElse(100);
        return (int) Math.round(average);
    }
}
