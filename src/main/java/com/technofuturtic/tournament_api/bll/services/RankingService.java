package com.technofuturtic.tournament_api.bll.services;

import com.technofuturtic.tournament_api.api.models.tournament.responses.ParticipantRankingResponse;

import java.util.List;

public interface RankingService {

    List<ParticipantRankingResponse> getRanking(Integer tournamentId);
}
