package com.technofuturtic.tournament_api.bll.services;

import com.technofuturtic.tournament_api.api.models.team.requests.TeamUpdateRequest;
import com.technofuturtic.tournament_api.dl.entities.TeamEntity;

public interface TeamService {

    TeamEntity create(String name, Integer creatorId);

    void update(Integer id, TeamUpdateRequest request);

    void delete(Integer id);
}
