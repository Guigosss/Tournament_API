package com.technofuturtic.tournament_api.api.models.team.responses;

import com.technofuturtic.tournament_api.dl.entities.TeamJoinRequestEntity;
import com.technofuturtic.tournament_api.dl.enums.InvitationStatus;
import java.time.LocalDateTime;

public record TeamJoinRequestResponse(Integer id, Integer teamId, String teamName,
                                      Integer playerId, String username, InvitationStatus status,
                                      LocalDateTime createdAt) {
    public static TeamJoinRequestResponse fromRequest(TeamJoinRequestEntity request) {
        return new TeamJoinRequestResponse(request.getId(), request.getTeam().getId(), request.getTeam().getName(),
                request.getPlayer().getId(), request.getPlayer().getUsername(), request.getStatus(), request.getCreatedAt());
    }
}