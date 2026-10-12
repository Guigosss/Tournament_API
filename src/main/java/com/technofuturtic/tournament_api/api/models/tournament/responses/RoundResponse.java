package com.technofuturtic.tournament_api.api.models.tournament.responses;

import com.technofuturtic.tournament_api.dl.entities.RoundEntity;
import com.technofuturtic.tournament_api.dl.enums.RoundType;

public record RoundResponse(
        Integer id,
        RoundType round,
        Integer orderIndex
) {

    public static RoundResponse fromEntity(RoundEntity round) {
        return new RoundResponse(
                round.getId(),
                round.getType(),
                round.getOrderIndex()
        );
    }
}
