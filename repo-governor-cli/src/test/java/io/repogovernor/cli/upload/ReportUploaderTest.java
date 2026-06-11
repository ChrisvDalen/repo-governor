package io.repogovernor.cli.upload;

import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ReportUploaderTest {

    private HttpServer server;
    private final AtomicReference<String> receivedApiKey = new AtomicReference<>();
    private final AtomicReference<String> receivedBody = new AtomicReference<>();

    @BeforeEach
    void startServer() throws IOException {
        server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/api/v1/scan-reports", exchange -> {
            receivedApiKey.set(exchange.getRequestHeaders().getFirst("X-API-Key"));
            receivedBody.set(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
            byte[] response = "{\"id\":\"scan-1\"}".getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(201, response.length);
            exchange.getResponseBody().write(response);
            exchange.close();
        });
        server.start();
    }

    @AfterEach
    void stopServer() {
        server.stop(0);
    }

    @Test
    void uploadsReportWithApiKeyHeader() {
        var uploader = new ReportUploader();
        String baseUrl = "http://localhost:" + server.getAddress().getPort();

        var result = uploader.upload(baseUrl, "dev-api-key", "{\"repositoryName\":\"demo\"}");

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.statusCode()).isEqualTo(201);
        assertThat(receivedApiKey.get()).isEqualTo("dev-api-key");
        assertThat(receivedBody.get()).contains("demo");
    }

    @Test
    void unreachableServerThrowsUploadException() {
        var uploader = new ReportUploader();

        assertThatThrownBy(() -> uploader.upload("http://localhost:1", "key", "{}"))
                .isInstanceOf(ReportUploader.UploadException.class)
                .hasMessageContaining("Could not reach server");
    }
}
