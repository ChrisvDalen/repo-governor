package io.repogovernor.server.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

/**
 * MVP authentication: every /api/** request must carry a valid X-API-Key
 * header. The key resolves to an organization, which scopes all data access.
 */
@Component
public class ApiKeyFilter extends OncePerRequestFilter {

    public static final String API_KEY_HEADER = "X-API-Key";
    public static final String ORGANIZATION_ATTRIBUTE = "repoGovernor.organizationId";

    private final ApiKeyAuthService apiKeyAuthService;

    public ApiKeyFilter(ApiKeyAuthService apiKeyAuthService) {
        this.apiKeyAuthService = apiKeyAuthService;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return !path.startsWith("/api/")
                || path.startsWith("/api/docs")
                || "OPTIONS".equals(request.getMethod());
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        Optional<UUID> organizationId = apiKeyAuthService.organizationIdForKey(request.getHeader(API_KEY_HEADER));
        if (organizationId.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"Missing or invalid X-API-Key header\"}");
            return;
        }
        request.setAttribute(ORGANIZATION_ATTRIBUTE, organizationId.get());
        chain.doFilter(request, response);
    }
}
