package io.repogovernor.server.repository;

import io.repogovernor.server.common.Category;
import io.repogovernor.server.common.Severity;
import io.repogovernor.server.finding.FindingDto;
import io.repogovernor.server.finding.FindingStatus;
import io.repogovernor.server.repository.RepositoryDtos.RegisterRepositoryRequest;
import io.repogovernor.server.repository.RepositoryDtos.RepositoryDetail;
import io.repogovernor.server.repository.RepositoryDtos.RepositorySummary;
import io.repogovernor.server.scan.ScanDtos.ScanSummary;
import io.repogovernor.server.scan.ScanQueryService;
import io.repogovernor.server.security.CallerOrganization;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/repositories")
public class RepositoryController {

    private final RepositoryService repositoryService;
    private final ScanQueryService scanQueryService;

    public RepositoryController(RepositoryService repositoryService, ScanQueryService scanQueryService) {
        this.repositoryService = repositoryService;
        this.scanQueryService = scanQueryService;
    }

    @GetMapping
    public List<RepositorySummary> list(HttpServletRequest request) {
        return repositoryService.list(CallerOrganization.id(request));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RepositorySummary register(@Valid @RequestBody RegisterRepositoryRequest body,
                                      HttpServletRequest request) {
        return repositoryService.register(CallerOrganization.id(request), body);
    }

    @GetMapping("/{repositoryId}")
    public RepositoryDetail get(@PathVariable UUID repositoryId, HttpServletRequest request) {
        return repositoryService.get(CallerOrganization.id(request), repositoryId);
    }

    @GetMapping("/{repositoryId}/scans")
    public List<ScanSummary> scans(@PathVariable UUID repositoryId, HttpServletRequest request) {
        repositoryService.get(CallerOrganization.id(request), repositoryId);
        return scanQueryService.listForRepository(repositoryId);
    }

    @GetMapping("/{repositoryId}/findings")
    public List<FindingDto> findings(@PathVariable UUID repositoryId,
                                     @RequestParam(required = false) Severity severity,
                                     @RequestParam(required = false) Category category,
                                     @RequestParam(required = false) FindingStatus status,
                                     HttpServletRequest request) {
        return repositoryService.findings(CallerOrganization.id(request), repositoryId, severity, category, status);
    }
}
