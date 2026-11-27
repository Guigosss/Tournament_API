package com.technofuturtic.tournament_api.bll.services;

import com.technofuturtic.tournament_api.bll.models.team.requests.TeamUpdateRequest;
import com.technofuturtic.tournament_api.dl.entities.TeamEntity;

import java.util.List;

public interface TeamService {
    java.util.List<TeamEntity> search(String name);

    TeamEntity findById(Integer id);

    TeamEntity create(String name, Integer creatorId);

    List<TeamEntity> findMine(Integer userId);

    void update(Integer id, TeamUpdateRequest request);

    void delete(Integer id);

}