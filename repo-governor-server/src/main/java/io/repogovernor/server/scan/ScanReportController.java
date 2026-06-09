package io.repogovernor.server.scan;

import io.repogovernor.server.scan.ScanDtos.ScanReportPayload;
import io.repogovernor.server.scan.ScanDtos.ScanUploadResult;
import io.repogovernor.server.security.CallerOrganization;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/scan-reports")
public class ScanReportController {

    private final ScanUploadService scanUploadService;

    public ScanReportController(ScanUploadService scanUploadService) {
        this.scanUploadService = scanUploadService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ScanUploadResult upload(@Valid @RequestBody ScanReportPayload payload, HttpServletRequest request) {
        return scanUploadService.store(CallerOrganization.id(request), payload);
    }
}
