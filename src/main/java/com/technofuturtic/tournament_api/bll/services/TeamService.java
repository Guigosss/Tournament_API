package com.technofuturtic.tournament_api.bll.services;

import com.technofuturtic.tournament_api.api.models.team.requests.TeamUpdateRequest;
import com.technofuturtic.tournament_api.dl.entities.TeamEntity;

public interface TeamService {
    java.util.List<TeamEntity> search(String name);

    TeamEntity findById(Integer id);

    TeamEntity create(String name, Integer creatorId);

    void update(Integer id, TeamUpdateRequest request);

    void delete(Integer id);
}