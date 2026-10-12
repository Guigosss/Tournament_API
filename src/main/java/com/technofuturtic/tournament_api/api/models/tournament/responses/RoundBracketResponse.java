package com.technofuturtic.tournament_api.api.models.tournament.responses;

import com.technofuturtic.tournament_api.dl.entities.RoundEntity;
import com.technofuturtic.tournament_api.dl.enums.RoundType;

import java.util.List;

public record RoundBracketResponse(
        Integer id,
        RoundType type,
        Integer orderIndex,
        List<MatchResponse> matches
) {
    public static RoundBracketResponse fromEntity(
            RoundEntity round,
            List<MatchResponse> matches
    ) {
        return new RoundBracketResponse(
                round.getId(),
                round.getType(),
                round.getOrderIndex(),
                matches
        );
    }
}
