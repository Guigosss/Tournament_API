package com.technofuturtic.tournament_api.api.models.team.responses;

import com.technofuturtic.tournament_api.dl.entities.TeamInvitationEntity;
import com.technofuturtic.tournament_api.dl.enums.InvitationStatus;
import java.time.LocalDateTime;

public record TeamInvitationResponse(Integer id, Integer teamId, String teamName,
                                     Integer playerId, InvitationStatus status, LocalDateTime createdAt, LocalDateTime acceptedAt) {
    public static TeamInvitationResponse fromInvitation(TeamInvitationEntity invitation) {
        return new TeamInvitationResponse(invitation.getId(), invitation.getTeam().getId(),
                invitation.getTeam().getName(), invitation.getPlayer().getId(), invitation.getStatus(),
                invitation.getCreatedAt(), invitation.getAcceptedAt());
    }
}