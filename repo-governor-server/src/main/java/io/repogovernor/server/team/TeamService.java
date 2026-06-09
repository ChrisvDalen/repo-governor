package io.repogovernor.server.team;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class TeamService {

    private final TeamRepository teamRepository;

    public TeamService(TeamRepository teamRepository) {
        this.teamRepository = teamRepository;
    }

    @Transactional(readOnly = true)
    public List<TeamDto> list(UUID organizationId) {
        return teamRepository.findByOrganizationId(organizationId).stream().map(TeamDto::from).toList();
    }

    public TeamDto create(UUID organizationId, String name) {
        return TeamDto.from(findOrCreate(organizationId, name));
    }

    public TeamEntity findOrCreate(UUID organizationId, String name) {
        return teamRepository.findByOrganizationIdAndName(organizationId, name)
                .orElseGet(() -> teamRepository.save(new TeamEntity(organizationId, name)));
    }

    public record TeamDto(UUID id, UUID organizationId, String name) {

        static TeamDto from(TeamEntity entity) {
            return new TeamDto(entity.getId(), entity.getOrganizationId(), entity.getName());
        }
    }
}
