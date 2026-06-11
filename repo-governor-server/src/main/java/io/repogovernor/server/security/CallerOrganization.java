package io.repogovernor.server.security;

import jakarta.servlet.http.HttpServletRequest;

import java.util.UUID;

/**
 * Resolves the organization of the authenticated caller, as established by
 * {@link ApiKeyFilter}.
 */
public final class CallerOrganization {

    private CallerOrganization() {
    }

    public static UUID id(HttpServletRequest request) {
        Object value = request.getAttribute(ApiKeyFilter.ORGANIZATION_ATTRIBUTE);
        if (!(value instanceof UUID organizationId)) {
            throw new IllegalStateException("Request is not authenticated with an API key");
        }
        return organizationId;
    }
}
