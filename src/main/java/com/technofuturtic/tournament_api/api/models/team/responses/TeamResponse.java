package com.technofuturtic.tournament_api.api.models.team.responses;

import com.technofuturtic.tournament_api.dl.entities.TeamEntity;
import java.util.List;

public record TeamResponse(Integer id, String name, Integer numberOfWins,
                           PlayerResponse captain, List<PlayerResponse> members) {
    public record PlayerResponse(Integer id, String username) {}

    public static TeamResponse fromTeam(TeamEntity team) {
        return new TeamResponse(team.getId(), team.getName(), team.getNumberOfWins(),
                new PlayerResponse(team.getCaptain().getId(), team.getCaptain().getUsername()),
                team.getMembers().stream()
                        .map(user -> new PlayerResponse(user.getId(), user.getUsername()))
                        .toList());
    }
}