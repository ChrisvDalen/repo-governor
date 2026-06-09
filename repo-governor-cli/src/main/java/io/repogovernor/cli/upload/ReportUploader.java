package io.repogovernor.cli.upload;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * Uploads a scan report (JSON) to the Repo Governor server.
 */
public final class ReportUploader {

    private final HttpClient httpClient;

    public ReportUploader() {
        this(HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build());
    }

    public ReportUploader(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    public UploadResult upload(String serverUrl, String apiKey, String reportJson) {
        String url = serverUrl.endsWith("/") ? serverUrl.substring(0, serverUrl.length() - 1) : serverUrl;
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url + "/api/v1/scan-reports"))
                .timeout(Duration.ofSeconds(30))
                .header("Content-Type", "application/json")
                .header("X-API-Key", apiKey)
                .POST(HttpRequest.BodyPublishers.ofString(reportJson))
                .build();
        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            return new UploadResult(response.statusCode(), response.body());
        } catch (IOException e) {
            throw new UploadException("Could not reach server at " + serverUrl + ": " + e.getMessage(), e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new UploadException("Upload interrupted", e);
        }
    }

    public record UploadResult(int statusCode, String body) {
        public boolean isSuccess() {
            return statusCode >= 200 && statusCode < 300;
        }
    }

    public static final class UploadException extends RuntimeException {
        public UploadException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
