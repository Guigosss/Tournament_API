package com.technofuturtic.tournament_api.bll.services;

public interface TeamMemberService {
    void remove(Integer teamId, Integer playerId, Integer actorId, boolean admin);
    void leave(Integer teamId, Integer playerId);
}