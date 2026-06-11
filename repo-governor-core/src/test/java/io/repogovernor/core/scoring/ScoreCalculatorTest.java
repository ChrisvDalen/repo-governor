package io.repogovernor.core.scoring;

import io.repogovernor.core.domain.Category;
import io.repogovernor.core.domain.CategoryScore;
import io.repogovernor.core.domain.Finding;
import io.repogovernor.core.domain.Severity;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class ScoreCalculatorTest {

    private final ScoreCalculator calculator = new ScoreCalculator();

    private static Finding finding(Category category, Severity severity) {
        return Finding.builder("test.rule").title("t").severity(severity).category(category).build();
    }

    @Test
    void cleanCategoryScoresHundred() {
        var scores = calculator.categoryScores(Set.of(Category.REPO_HEALTH), List.of());

        assertThat(scores).containsExactly(new CategoryScore(Category.REPO_HEALTH, 100));
    }

    @Test
    void blockerLowersScoreStronglyAndMajorModerately() {
        var scores = calculator.categoryScores(Set.of(Category.SECURITY, Category.JAVA), List.of(
                finding(Category.SECURITY, Severity.BLOCKER),
                finding(Category.JAVA, Severity.MAJOR)));

        assertThat(scores).containsExactlyInAnyOrder(
                new CategoryScore(Category.SECURITY, 55),
                new CategoryScore(Category.JAVA, 85));
    }

    @Test
    void scoreNeverGoesBelowZero() {
        var findings = List.of(
                finding(Category.SECURITY, Severity.BLOCKER),
                finding(Category.SECURITY, Severity.BLOCKER),
                finding(Category.SECURITY, Severity.BLOCKER));

        var scores = calculator.categoryScores(Set.of(Category.SECURITY), findings);

        assertThat(scores.getFirst().score()).isZero();
    }

    @Test
    void irrelevantCategoriesAreNotScored() {
        var scores = calculator.categoryScores(Set.of(Category.REPO_HEALTH),
                List.of(finding(Category.ANGULAR, Severity.BLOCKER)));

        assertThat(scores).extracting(CategoryScore::category).containsExactly(Category.REPO_HEALTH);
    }

    @Test
    void overallScoreIsAverageOfRelevantCategories() {
        int overall = calculator.overallScore(List.of(
                new CategoryScore(Category.REPO_HEALTH, 100),
                new CategoryScore(Category.JAVA, 50)));

        assertThat(overall).isEqualTo(75);
    }
}
