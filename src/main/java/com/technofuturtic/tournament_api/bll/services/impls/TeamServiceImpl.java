package com.technofuturtic.tournament_api.bll.services.impls;

import com.technofuturtic.tournament_api.api.models.team.requests.TeamUpdateRequest;
import com.technofuturtic.tournament_api.bll.exceptions.team.TeamConflictException;
import com.technofuturtic.tournament_api.bll.exceptions.team.TeamNotFoundException;
import com.technofuturtic.tournament_api.bll.exceptions.user.UserNotFoundException;
import com.technofuturtic.tournament_api.bll.services.TeamService;
import com.technofuturtic.tournament_api.dal.repositories.TeamRepository;
import com.technofuturtic.tournament_api.dal.repositories.UserRepository;
import com.technofuturtic.tournament_api.dl.entities.TeamEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TeamServiceImpl implements TeamService {

    private final TeamRepository teamRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public java.util.List<TeamEntity> search(String name) {
        if (name == null || name.isBlank() || name.strip().length() > 50) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.BAD_REQUEST, "Le nom recherché doit contenir entre 1 et 50 caractères.");
        }
        return teamRepository.findTop20ByNameContainingIgnoreCaseOrderByNameAsc(name.strip());
    }

    @Override
    @Transactional(readOnly = true)
    public TeamEntity findById(Integer id) {
        return teamRepository.findWithMembersById(id)
                .orElseThrow(() -> new TeamNotFoundException("Team not found, Id : " + id));
    }

    @Override
    @Transactional
    public TeamEntity create(String name, Integer creatorId) {

        var creator = userRepository.findByIdForUpdate(creatorId)
                .filter(user -> !user.isDeleted())
                .orElseThrow(() ->
                        new UserNotFoundException("Player not found"));

        String teamName = name.strip();

        if (teamRepository.existsByName(teamName)) {
            throw new TeamConflictException(
                    "name",
                    "Team name already exists"
            );
        }

        var team = new TeamEntity();
        team.setName(teamName);
        team.setCaptain(creator);
        team.addMember(creator);

        return teamRepository.saveAndFlush(team);
    }

    @Override
    @Transactional
    public void update(Integer id, TeamUpdateRequest request) {

        TeamEntity existingTeam = teamRepository.findByIdForUpdate(id)
                .orElseThrow(() ->
                        new TeamNotFoundException(
                                "Team not found, Id : " + id
                        ));

        if (existingTeam.isArchived()) {
            throw new TeamConflictException("team", "Une équipe archivée ne peut plus être modifiée.");
        }

        String teamName = request.name();

        if (!existingTeam.getName().equals(teamName)
                && teamRepository.existsByName(teamName)) {

            throw new TeamConflictException(
                    "name",
                    "Team name already exists"
            );
        }

        var captain = userRepository.findById(request.captainId())
                .filter(user -> !user.isDeleted())
                .orElseThrow(() ->
                        new UserNotFoundException("Captain not found"));

        boolean isMember = false;
        for (var member : existingTeam.getMembers()) {
            if (request.captainId().equals(member.getId())) {
                isMember = true;
                break;
            }
        }
        if (!isMember) {
            throw new TeamConflictException(
                    "captainId",
                    "Captain must already be a member of the team"
            );
        }

        existingTeam.setName(teamName);
        existingTeam.setCaptain(captain);

        teamRepository.save(existingTeam);
    }

    @Override
    @Transactional
    public void delete(Integer id) {

        TeamEntity team = teamRepository.findByIdForUpdate(id)
                .orElseThrow(() ->
                        new TeamNotFoundException(
                                "Team not found, Id : " + id
                        ));

        team.getMembers().clear();
        team.setCaptain(null);
        team.setArchived(true);
        teamRepository.saveAndFlush(team);
    }
}