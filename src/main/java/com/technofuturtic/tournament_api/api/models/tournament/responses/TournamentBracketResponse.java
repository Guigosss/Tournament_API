package com.technofuturtic.tournament_api.api.models.tournament.responses;

import com.technofuturtic.tournament_api.dl.entities.TournamentEntity;

import java.util.List;

public record TournamentBracketResponse(
        Integer tournamentId,
        List<PhaseBracketResponse> phases
) {

    public static TournamentBracketResponse fromEntity(
            TournamentEntity tournament,
            List<PhaseBracketResponse> phases
    ) {
        return new TournamentBracketResponse(
                tournament.getId(),
                phases
        );
    }
}
