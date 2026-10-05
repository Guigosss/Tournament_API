package com.technofuturtic.tournament_api.api.models.tournament.responses;

import com.technofuturtic.tournament_api.dl.entities.ParticipantEntity;

public record ParticipantResponse(
        Integer id,
        Integer userId,
        Integer teamId
) {

    public static ParticipantResponse fromEntity(ParticipantEntity participant) {
        if (participant == null) {
            return null;
        }

        return new ParticipantResponse(
                participant.getId(),
                participant.getUser() != null
                        ? participant.getUser().getId()
                        : null,
                participant.getTeam() != null
                        ? participant.getTeam().getId()
                        : null
        );
    }
}
