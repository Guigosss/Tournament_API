package com.technofuturtic.tournament_api.bll.services;

import com.technofuturtic.tournament_api.dl.entities.MatchEntity;

public interface TournamentProgressionService {

    void progress(MatchEntity match);
}
