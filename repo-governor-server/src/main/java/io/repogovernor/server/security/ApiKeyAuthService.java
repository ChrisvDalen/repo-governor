package io.repogovernor.server.security;

import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class ApiKeyAuthService {

    private final ApiKeyRepository apiKeyRepository;

    public ApiKeyAuthService(ApiKeyRepository apiKeyRepository) {
        this.apiKeyRepository = apiKeyRepository;
    }

    public Optional<UUID> organizationIdForKey(String apiKey) {
        if (apiKey == null || apiKey.isBlank()) {
            return Optional.empty();
        }
        return apiKeyRepository.findByKeyValue(apiKey).map(ApiKeyEntity::getOrganizationId);
    }
}
