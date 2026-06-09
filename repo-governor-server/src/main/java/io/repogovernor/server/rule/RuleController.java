package io.repogovernor.server.rule;

import io.repogovernor.server.rule.RuleService.RuleWithConfiguration;
import io.repogovernor.server.security.CallerOrganization;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/rules")
public class RuleController {

    private final RuleService ruleService;

    public RuleController(RuleService ruleService) {
        this.ruleService = ruleService;
    }

    @GetMapping
    public List<RuleWithConfiguration> list(HttpServletRequest request) {
        return ruleService.list(CallerOrganization.id(request));
    }

    @PatchMapping("/{ruleId}/configuration")
    public RuleWithConfiguration updateConfiguration(@PathVariable UUID ruleId,
                                                     @RequestBody UpdateRuleConfigurationRequest body,
                                                     HttpServletRequest request) {
        return ruleService.updateConfiguration(CallerOrganization.id(request), ruleId,
                body.enabled(), body.threshold());
    }

    public record UpdateRuleConfigurationRequest(Boolean enabled, Integer threshold) {
    }
}
