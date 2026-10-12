package com.technofuturtic.tournament_api.bll.services;

import com.technofuturtic.tournament_api.api.models.team.responses.TeamInvitationResponse;
import java.util.List;

public interface TeamInvitationService {
    TeamInvitationResponse invite(Integer teamId, Integer playerId, Integer senderId, boolean admin);
    List<TeamInvitationResponse> findMyInvitations(Integer playerId);
    TeamInvitationResponse accept(Integer invitationId, Integer playerId);
    TeamInvitationResponse reject(Integer invitationId, Integer playerId);
    TeamInvitationResponse cancel(Integer invitationId, Integer userId, boolean admin);
}