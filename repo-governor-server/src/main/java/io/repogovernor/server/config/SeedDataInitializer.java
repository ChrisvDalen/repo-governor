package io.repogovernor.server.config;

import io.repogovernor.server.organization.OrganizationEntity;
import io.repogovernor.server.organization.OrganizationRepository;
import io.repogovernor.server.security.ApiKeyEntity;
import io.repogovernor.server.security.ApiKeyRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Seeds a development organization and API key so the vertical slice works out
 * of the box: CLI uploads with 'dev-api-key', dashboard reads with the same key.
 */
@Configuration
@ConditionalOnProperty(name = "repo-governor.seed.enabled", havingValue = "true")
public class SeedDataInitializer {

    private static final Logger log = LoggerFactory.getLogger(SeedDataInitializer.class);

    public static final String DEV_ORGANIZATION = "demo-org";
    public static final String DEV_API_KEY = "dev-api-key";

    @Bean
    ApplicationRunner seedDevData(OrganizationRepository organizationRepository,
                                  ApiKeyRepository apiKeyRepository) {
        return args -> {
            OrganizationEntity organization = organizationRepository.findByName(DEV_ORGANIZATION)
                    .orElseGet(() -> organizationRepository.save(new OrganizationEntity(DEV_ORGANIZATION)));
            if (apiKeyRepository.findByKeyValue(DEV_API_KEY).isEmpty()) {
                apiKeyRepository.save(new ApiKeyEntity(organization.getId(), DEV_API_KEY, "development key"));
                log.info("Seeded organization '{}' with API key '{}'", DEV_ORGANIZATION, DEV_API_KEY);
            }
        };
    }
}
