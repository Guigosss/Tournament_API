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
    @Transactional
    public TeamEntity create(String name, Integer creatorId) {

        var creator = userRepository.findById(creatorId)
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

        TeamEntity existingTeam = teamRepository.findById(id)
                .orElseThrow(() ->
                        new TeamNotFoundException(
                                "Team not found, Id : " + id
                        ));

        String teamName = request.name();

        if (!existingTeam.getName().equals(teamName)
                && teamRepository.existsByName(teamName)) {

            throw new TeamConflictException(
                    "name",
                    "Team name already exists"
            );
        }

        existingTeam.setName(teamName);

        var captain = userRepository.findById(request.captainId())
                .orElseThrow(() ->
                        new UserNotFoundException("Captain not found"));

        existingTeam.setCaptain(captain);

        teamRepository.save(existingTeam);
    }

    @Override
    @Transactional
    public void delete(Integer id) {

        TeamEntity team = teamRepository.findById(id)
                .orElseThrow(() ->
                        new TeamNotFoundException(
                                "Team not found, Id : " + id
                        ));

        team.getMembers().clear();

        teamRepository.delete(team);
    }
}