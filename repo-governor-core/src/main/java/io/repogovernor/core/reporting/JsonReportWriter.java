package io.repogovernor.core.reporting;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.repogovernor.core.domain.ScanReport;

import java.io.IOException;
import java.io.UncheckedIOException;

/**
 * Serializes a {@link ScanReport} to JSON. This format is both the on-disk
 * report and the upload payload for the server.
 */
public final class JsonReportWriter {

    private final ObjectMapper objectMapper;

    public JsonReportWriter() {
        this.objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .enable(SerializationFeature.INDENT_OUTPUT);
    }

    public String write(ScanReport report) {
        try {
            return objectMapper.writeValueAsString(report);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to serialize scan report", e);
        }
    }

    public ScanReport read(String json) {
        try {
            return objectMapper.readValue(json, ScanReport.class);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to parse scan report", e);
        }
    }
}
