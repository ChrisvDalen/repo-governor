package io.repogovernor.server.organization;

import io.repogovernor.server.common.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class OrganizationService {

    private final OrganizationRepository organizationRepository;

    public OrganizationService(OrganizationRepository organizationRepository) {
        this.organizationRepository = organizationRepository;
    }

    @Transactional(readOnly = true)
    public List<OrganizationDto> list() {
        return organizationRepository.findAll().stream().map(OrganizationDto::from).toList();
    }

    @Transactional(readOnly = true)
    public OrganizationDto get(UUID id) {
        return organizationRepository.findById(id)
                .map(OrganizationDto::from)
                .orElseThrow(() -> new NotFoundException("Organization", id));
    }

    public OrganizationDto create(String name) {
        organizationRepository.findByName(name).ifPresent(existing -> {
            throw new IllegalArgumentException("Organization '" + name + "' already exists");
        });
        return OrganizationDto.from(organizationRepository.save(new OrganizationEntity(name)));
    }
}
