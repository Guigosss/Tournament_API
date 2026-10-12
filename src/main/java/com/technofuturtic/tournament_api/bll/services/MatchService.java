package com.technofuturtic.tournament_api.bll.services;

import com.technofuturtic.tournament_api.api.models.tournament.requests.MatchResultRequest;
import com.technofuturtic.tournament_api.api.models.tournament.responses.MatchResponse;

public interface MatchService {

    MatchResponse submitResult(Integer matchId, MatchResultRequest request);
}
