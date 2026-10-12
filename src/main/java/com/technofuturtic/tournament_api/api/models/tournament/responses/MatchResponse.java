package com.technofuturtic.tournament_api.api.models.tournament.responses;

import com.technofuturtic.tournament_api.dl.entities.MatchEntity;
import com.technofuturtic.tournament_api.dl.enums.MatchStatus;

import java.time.LocalDate;

public record MatchResponse(
        Integer id,
        BracketParticipantResponse participant1,
        BracketParticipantResponse participant2,
        Integer score1,
        Integer score2,
        BracketParticipantResponse winner,
        MatchStatus status,
        Integer orderIndex,
        LocalDate scheduledDate
) {

    public static MatchResponse fromEntity(MatchEntity match) {
        return new MatchResponse(
                match.getId(),
                BracketParticipantResponse.fromEntity(match.getParticipant1()),
                BracketParticipantResponse.fromEntity(match.getParticipant2()),
                match.getScore1(),
                match.getScore2(),
                match.getWinner() != null
                        ? BracketParticipantResponse.fromEntity(match.getWinner())
                        : null,
                match.getStatus(),
                match.getOrderIndex(),
                match.getScheduledDate()
        );
    }
}
