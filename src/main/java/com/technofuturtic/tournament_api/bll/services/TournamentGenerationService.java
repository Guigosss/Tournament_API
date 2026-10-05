package com.technofuturtic.tournament_api.bll.services;

import com.technofuturtic.tournament_api.api.models.tournament.responses.MatchResponse;
import com.technofuturtic.tournament_api.api.models.tournament.responses.PhaseResponse;
import com.technofuturtic.tournament_api.api.models.tournament.responses.RoundResponse;
import com.technofuturtic.tournament_api.api.models.tournament.responses.TournamentBracketResponse;

import java.util.List;

public interface TournamentGenerationService {

    TournamentBracketResponse getBracket(Integer tournamentId);
    List<PhaseResponse> getPhases(Integer tournamentId);
    List<RoundResponse> getRounds(Integer tournamentId, Integer phaseId);
    List<MatchResponse> getMatches(Integer tournamentId, Integer phaseId, Integer roundId);
}
