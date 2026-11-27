package com.technofuturtic.tournament_api.bll.services;

import com.technofuturtic.tournament_api.bll.models.tournament.requests.MatchResultRequest;
import com.technofuturtic.tournament_api.bll.models.tournament.responses.MatchResponse;
import com.technofuturtic.tournament_api.bll.models.tournament.responses.UserMatchResponse;

import java.util.List;

public interface MatchService {

    MatchResponse submitResult(Integer matchId, MatchResultRequest request);
    List<UserMatchResponse> getUserMatches(Integer userId);
}
