package com.technofuturtic.tournament_api.api.models.team.responses;

import com.technofuturtic.tournament_api.dl.entities.TeamEntity;
import java.util.List;

public record TeamResponse(Integer id, String name, Integer numberOfWins,
                           PlayerResponse captain, List<PlayerResponse> members, boolean archived) {
    public record PlayerResponse(Integer id, String username) {}

    public static TeamResponse fromTeam(TeamEntity team) {
        List<PlayerResponse> members = new java.util.ArrayList<>();
        for (var user : team.getMembers()) {
            members.add(new PlayerResponse(user.getId(), user.getUsername()));
        }
        PlayerResponse captain = null;
        if (team.getCaptain() != null) {
            captain = new PlayerResponse(team.getCaptain().getId(), team.getCaptain().getUsername());
        }
        return new TeamResponse(team.getId(), team.getName(), team.getNumberOfWins(), captain,
                members, team.isArchived());
    }
}