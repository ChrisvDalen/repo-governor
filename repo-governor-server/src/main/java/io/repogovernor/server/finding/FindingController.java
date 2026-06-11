package io.repogovernor.server.finding;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/findings")
public class FindingController {

    private final FindingService findingService;

    public FindingController(FindingService findingService) {
        this.findingService = findingService;
    }

    @PatchMapping("/{findingId}/status")
    public FindingDto updateStatus(@PathVariable UUID findingId,
                                   @Valid @RequestBody UpdateFindingStatusRequest request) {
        return findingService.updateStatus(findingId, request.status());
    }

    public record UpdateFindingStatusRequest(@NotNull FindingStatus status) {
    }
}
