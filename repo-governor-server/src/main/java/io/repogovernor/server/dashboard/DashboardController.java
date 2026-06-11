package io.repogovernor.server.dashboard;

import io.repogovernor.server.dashboard.DashboardService.DashboardSummary;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/organizations/{organizationId}/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping
    public DashboardSummary summary(@PathVariable UUID organizationId) {
        return dashboardService.summary(organizationId);
    }
}
