package com.technofuturtic.tournament_api.bll.services;

import com.technofuturtic.tournament_api.api.models.team.responses.TeamJoinRequestResponse;
import java.util.List;

public interface TeamJoinRequestService {
    TeamJoinRequestResponse submit(Integer teamId, Integer playerId);
    List<TeamJoinRequestResponse> findMine(Integer playerId);
    List<TeamJoinRequestResponse> findForTeam(Integer teamId, Integer actorId, boolean admin);
    TeamJoinRequestResponse accept(Integer requestId, Integer actorId, boolean admin);
    TeamJoinRequestResponse reject(Integer requestId, Integer actorId, boolean admin);
    TeamJoinRequestResponse cancel(Integer requestId, Integer playerId);
}