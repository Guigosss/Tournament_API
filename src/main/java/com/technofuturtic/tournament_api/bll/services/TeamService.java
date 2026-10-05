package com.technofuturtic.tournament_api.bll.services;

import com.technofuturtic.tournament_api.dl.entities.TeamEntity;

public interface TeamService {
    TeamEntity create(String name, Integer creatorId);
}