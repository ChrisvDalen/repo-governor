package io.repogovernor.server.scan;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * End-to-end vertical slice: upload a CLI scan report, read it back through
 * the dashboard APIs, triage a finding, upload a second scan and diff them.
 */
@Testcontainers(disabledWithoutDocker = true)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ScanUploadFlowIT {

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:16-alpine");

    @LocalServerPort
    int port;

    private RestClient client() {
        return RestClient.builder()
                .baseUrl("http://localhost:" + port)
                .defaultHeader("X-API-Key", "dev-api-key")
                .build();
    }

    private Map<String, Object> payload(int overallScore, List<Map<String, Object>> findings) {
        return Map.of(
                "repositoryName", "demo-service",
                "organization", "demo-org",
                "team", "platform",
                "branch", "main",
                "commitHash", "abc123",
                "scannedAt", java.time.Instant.now().toString(),
                "toolVersion", "0.1.0",
                "overallScore", overallScore,
                "categoryScores", List.of(
                        Map.of("category", "REPO_HEALTH", "score", overallScore),
                        Map.of("category", "SECURITY", "score", 80)),
                "findings", findings);
    }

    private static Map<String, Object> finding(String ruleId, String severity) {
        return Map.of(
                "ruleId", ruleId,
                "title", "Title for " + ruleId,
                "description", "Description",
                "severity", severity,
                "category", "REPO_HEALTH",
                "recommendation", "Fix it");
    }

    @Test
    @Order(1)
    void rejectsRequestsWithoutApiKey() {
        var response = RestClient.builder().baseUrl("http://localhost:" + port).build()
                .get().uri("/api/v1/repositories")
                .retrieve()
                .onStatus(status -> true, (req, res) -> {
                })
                .toBodilessEntity();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    @Order(2)
    @SuppressWarnings("unchecked")
    void uploadTriageAndDiffFlow() {
        // 1. Upload the first scan report.
        Map<String, Object> uploadResult = client().post().uri("/api/v1/scan-reports")
                .contentType(MediaType.APPLICATION_JSON)
                .body(payload(60, List.of(
                        finding("repo.readme.exists", "MAJOR"),
                        finding("repo.gitignore.exists", "MAJOR"))))
                .retrieve()
                .body(Map.class);
        assertThat(uploadResult).containsKeys("scanId", "repositoryId");
        String repositoryId = (String) uploadResult.get("repositoryId");

        // 2. The repository appears in the list with its latest score.
        List<Map<String, Object>> repositories =
                client().get().uri("/api/v1/repositories").retrieve().body(List.class);
        assertThat(repositories).hasSize(1);
        assertThat(repositories.getFirst())
                .containsEntry("name", "demo-service")
                .containsEntry("teamName", "platform")
                .containsEntry("latestScore", 60);

        // 3. Findings can be filtered by severity.
        List<Map<String, Object>> findings = client().get()
                .uri("/api/v1/repositories/{id}/findings?severity=MAJOR", repositoryId)
                .retrieve().body(List.class);
        assertThat(findings).hasSize(2);

        // 4. Triage one finding as accepted risk.
        String findingId = (String) findings.getFirst().get("id");
        Map<String, Object> updated = client().patch()
                .uri("/api/v1/findings/{id}/status", findingId)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("status", "ACCEPTED_RISK"))
                .retrieve().body(Map.class);
        assertThat(updated).containsEntry("status", "ACCEPTED_RISK");
        String triagedRuleId = (String) updated.get("ruleId");

        // 5. Upload a second scan: gitignore fixed, env file introduced.
        Map<String, Object> secondUpload = client().post().uri("/api/v1/scan-reports")
                .contentType(MediaType.APPLICATION_JSON)
                .body(payload(75, List.of(
                        finding("repo.readme.exists", "MAJOR"),
                        finding("security.env.not-committed", "BLOCKER"))))
                .retrieve().body(Map.class);
        String secondScanId = (String) secondUpload.get("scanId");

        // 6. The diff shows the new and resolved findings and the score delta.
        Map<String, Object> diff = client().get()
                .uri("/api/v1/scans/{id}/diff", secondScanId)
                .retrieve().body(Map.class);
        assertThat((Integer) diff.get("scoreDelta")).isEqualTo(15);
        List<Map<String, Object>> newFindings = (List<Map<String, Object>>) diff.get("newFindings");
        List<Map<String, Object>> resolvedFindings = (List<Map<String, Object>>) diff.get("resolvedFindings");
        assertThat(newFindings).extracting(f -> f.get("ruleId"))
                .containsExactly("security.env.not-committed");
        assertThat(resolvedFindings).extracting(f -> f.get("ruleId"))
                .containsExactly("repo.gitignore.exists");

        // 7. Triage status carried over to the matching finding in the new scan.
        List<Map<String, Object>> latestFindings = client().get()
                .uri("/api/v1/repositories/{id}/findings", repositoryId)
                .retrieve().body(List.class);
        assertThat(latestFindings)
                .filteredOn(f -> triagedRuleId.equals(f.get("ruleId")))
                .singleElement()
                .satisfies(f -> assertThat(f).containsEntry("status", "ACCEPTED_RISK"));

        // 8. Dashboard aggregates the organization.
        List<Map<String, Object>> organizations =
                client().get().uri("/api/v1/organizations").retrieve().body(List.class);
        String organizationId = (String) organizations.getFirst().get("id");
        Map<String, Object> dashboard = client().get()
                .uri("/api/v1/organizations/{id}/dashboard", organizationId)
                .retrieve().body(Map.class);
        assertThat(dashboard)
                .containsEntry("repositoryCount", 1)
                .containsEntry("overallScore", 75);
        assertThat((List<?>) dashboard.get("improvingRepositories")).hasSize(1);

        // 9. Rules are listed with default configuration and can be configured.
        List<Map<String, Object>> rules = client().get().uri("/api/v1/rules").retrieve().body(List.class);
        assertThat(rules).hasSizeGreaterThanOrEqualTo(20);
        String ruleId = (String) rules.getFirst().get("id");
        Map<String, Object> ruleConfig = client().patch()
                .uri("/api/v1/rules/{id}/configuration", ruleId)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("enabled", false))
                .retrieve().body(Map.class);
        assertThat(ruleConfig).containsEntry("enabled", false);
    }
}
