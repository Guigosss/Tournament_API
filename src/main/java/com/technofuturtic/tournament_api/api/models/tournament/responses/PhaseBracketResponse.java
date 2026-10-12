package com.technofuturtic.tournament_api.api.models.tournament.responses;

import com.technofuturtic.tournament_api.dl.entities.PhaseEntity;
import com.technofuturtic.tournament_api.dl.enums.PhaseType;

import java.util.List;

public record PhaseBracketResponse(
        Integer id,
        PhaseType type,
        Integer orderIndex,
        List<RoundBracketResponse> rounds
) {

    public static PhaseBracketResponse fromEntity(
            PhaseEntity phase,
            List<RoundBracketResponse> rounds
    ) {
        return new PhaseBracketResponse(
                phase.getId(),
                phase.getType(),
                phase.getOrderIndex(),
                rounds
        );
    }
}
