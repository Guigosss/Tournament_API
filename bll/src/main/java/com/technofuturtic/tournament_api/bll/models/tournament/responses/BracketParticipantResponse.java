package com.technofuturtic.tournament_api.bll.models.tournament.responses;

import com.technofuturtic.tournament_api.dl.entities.ParticipantEntity;

public record BracketParticipantResponse(
        Integer id,
        Integer userId,
        String username,
        Integer teamId,
        String teamName
) {
    public static BracketParticipantResponse fromEntity(ParticipantEntity participant) {
        if (participant == null) {
            return null;
        }

        return new BracketParticipantResponse(
                participant.getId(),
                participant.getUser() != null
                        ? participant.getUser().getId()
                        : null,
                participant.getUser() != null
                        ? participant.getUser().getUsername()
                        : null,
                participant.getTeam() != null
                        ? participant.getTeam().getId()
                        : null,
                participant.getTeam() != null
                        ? participant.getTeam().getName()
                        : null
        );
    }
}
