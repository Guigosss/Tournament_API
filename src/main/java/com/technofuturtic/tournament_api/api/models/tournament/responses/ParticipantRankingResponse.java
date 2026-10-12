package com.technofuturtic.tournament_api.api.models.tournament.responses;

import com.technofuturtic.tournament_api.dl.entities.ParticipantEntity;

public record ParticipantRankingResponse(
        Integer participantId,
        Integer userId,
        Integer teamId,
        String username,
        Integer rank
) {
    public static ParticipantRankingResponse fromEntity(ParticipantEntity participant, Integer rank) {
        if (participant == null) {
            return null;
        }

        return new ParticipantRankingResponse(
                participant.getId(),
                participant.getUser() != null
                        ? participant.getUser().getId()
                        : null,
                participant.getTeam() != null
                        ? participant.getTeam().getId()
                        : null,
                participant.getUser() != null
                        ? participant.getUser().getUsername()
                        : participant.getTeam() != null
                                ? participant.getTeam().getName()
                                : null,
                rank
        );
    }
}
