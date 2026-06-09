package io.repogovernor.core.scanner;

import io.repogovernor.core.domain.Category;
import io.repogovernor.core.domain.CategoryScore;
import io.repogovernor.core.domain.ScanReport;
import io.repogovernor.core.reporting.JsonReportWriter;
import io.repogovernor.core.rules.Rules;
import io.repogovernor.core.scanner.RepoScanner.ScanContext;
import io.repogovernor.core.testsupport.InMemoryRepoFiles;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RepoScannerTest {

    private final RepoScanner scanner = new RepoScanner(Rules.defaultRules());

    @Test
    void scanOfMinimalRepoProducesFindingsAndScores() {
        var repo = InMemoryRepoFiles.repo().withFile("README.md", "# Demo\nInstall, run, test instructions.");

        ScanReport report = scanner.scan(repo, ScanContext.of("demo-org", "platform", "0.1.0"));

        assertThat(report.repositoryName()).isEqualTo("test-repo");
        assertThat(report.organization()).isEqualTo("demo-org");
        assertThat(report.findings()).isNotEmpty();
        assertThat(report.categoryScores())
                .extracting(CategoryScore::category)
                .contains(Category.REPO_HEALTH, Category.SECURITY)
                .doesNotContain(Category.JAVA, Category.ANGULAR, Category.SPRING_BOOT);
        assertThat(report.overallScore()).isBetween(0, 100);
    }

    @Test
    void javaCategoriesBecomeRelevantForJavaRepos() {
        var repo = InMemoryRepoFiles.repo()
                .withFile("README.md", "install run test")
                .withFile("pom.xml", "<project>junit archunit</project>");

        ScanReport report = scanner.scan(repo, ScanContext.of("demo-org", null, "0.1.0"));

        assertThat(report.categoryScores())
                .extracting(CategoryScore::category)
                .contains(Category.JAVA);
        assertThat(report.detectedTechnologies()).contains("java", "maven");
    }

    @Test
    void gitBranchAndCommitAreReadFromDotGit() {
        var repo = InMemoryRepoFiles.repo()
                .withFile(".git/HEAD", "ref: refs/heads/main\n")
                .withFile(".git/refs/heads/main", "abc123def456\n");

        ScanReport report = scanner.scan(repo, ScanContext.of("demo-org", null, "0.1.0"));

        assertThat(report.branch()).isEqualTo("main");
        assertThat(report.commitHash()).isEqualTo("abc123def456");
    }

    @Test
    void reportSurvivesJsonRoundTrip() {
        var repo = InMemoryRepoFiles.repo().withFile("pom.xml", "<project/>");
        ScanReport report = scanner.scan(repo, ScanContext.of("demo-org", "platform", "0.1.0"));

        var writer = new JsonReportWriter();
        ScanReport parsed = writer.read(writer.write(report));

        assertThat(parsed).isEqualTo(report);
    }
}
