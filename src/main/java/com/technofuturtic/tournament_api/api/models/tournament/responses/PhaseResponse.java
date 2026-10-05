package com.technofuturtic.tournament_api.api.models.tournament.responses;

import com.technofuturtic.tournament_api.dl.entities.PhaseEntity;
import com.technofuturtic.tournament_api.dl.enums.PhaseType;

public record PhaseResponse(
        Integer id,
        PhaseType type,
        Integer orderIndex
) {

    public static PhaseResponse fromEntity(PhaseEntity phase) {
        return new PhaseResponse(
                phase.getId(),
                phase.getType(),
                phase.getOrderIndex()
        );
    }
}
