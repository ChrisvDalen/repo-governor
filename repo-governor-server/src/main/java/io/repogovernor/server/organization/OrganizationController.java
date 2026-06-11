package io.repogovernor.server.organization;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/organizations")
public class OrganizationController {

    private final OrganizationService organizationService;

    public OrganizationController(OrganizationService organizationService) {
        this.organizationService = organizationService;
    }

    @GetMapping
    public List<OrganizationDto> list() {
        return organizationService.list();
    }

    @GetMapping("/{organizationId}")
    public OrganizationDto get(@PathVariable UUID organizationId) {
        return organizationService.get(organizationId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrganizationDto create(@Valid @RequestBody CreateOrganizationRequest request) {
        return organizationService.create(request.name());
    }

    public record CreateOrganizationRequest(@NotBlank String name) {
    }
}
