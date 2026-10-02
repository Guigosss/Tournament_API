package com.technofuturtic.tournament_api.api.models.tournament.responses;

import com.technofuturtic.tournament_api.dl.entities.MatchEntity;
import com.technofuturtic.tournament_api.dl.enums.MatchStatus;

import java.time.LocalDate;

public record MatchResponse(
        Integer id,
        ParticipantResponse participant1,
        ParticipantResponse participant2,
        Integer score1,
        Integer score2,
        ParticipantResponse winner,
        MatchStatus status,
        Integer orderIndex,
        LocalDate scheduledDate
) {

    public static MatchResponse fromEntity(MatchEntity match) {
        return new MatchResponse(
                match.getId(),
                ParticipantResponse.fromEntity(match.getParticipant1()),
                ParticipantResponse.fromEntity(match.getParticipant2()),
                match.getScore1(),
                match.getScore2(),
                match.getWinner() != null
                        ? ParticipantResponse.fromEntity(match.getWinner())
                        : null,
                match.getStatus(),
                match.getOrderIndex(),
                match.getScheduledDate()
        );
    }
}
