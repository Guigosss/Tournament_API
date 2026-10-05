package com.technofuturtic.tournament_api.bll.services.impls;

import com.technofuturtic.tournament_api.bll.exceptions.team.TeamConflictException;
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
                .orElseThrow(() -> new UserNotFoundException("Player not found"));
        String teamName = name.strip();
        if (teamRepository.existsByName(teamName)) {
            throw new TeamConflictException("name", "Team name already exists");
        }
        var team = new TeamEntity();
        team.setName(teamName);
        team.setCaptain(creator);
        team.addMember(creator);
        return teamRepository.saveAndFlush(team);
    }
}