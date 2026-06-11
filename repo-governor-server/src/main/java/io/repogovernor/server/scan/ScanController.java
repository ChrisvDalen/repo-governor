package io.repogovernor.server.scan;

import io.repogovernor.server.scan.ScanDtos.ScanDetail;
import io.repogovernor.server.scan.ScanDtos.ScanDiff;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/scans")
public class ScanController {

    private final ScanQueryService scanQueryService;

    public ScanController(ScanQueryService scanQueryService) {
        this.scanQueryService = scanQueryService;
    }

    @GetMapping("/{scanId}")
    public ScanDetail get(@PathVariable UUID scanId) {
        return scanQueryService.get(scanId);
    }

    @GetMapping("/{scanId}/diff")
    public ScanDiff diff(@PathVariable UUID scanId) {
        return scanQueryService.diff(scanId);
    }
}
