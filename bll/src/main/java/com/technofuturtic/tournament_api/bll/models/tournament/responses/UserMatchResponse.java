package com.technofuturtic.tournament_api.bll.models.tournament.responses;

import com.technofuturtic.tournament_api.dl.entities.MatchEntity;
import com.technofuturtic.tournament_api.dl.entities.ParticipantEntity;
import com.technofuturtic.tournament_api.dl.entities.TournamentEntity;
import com.technofuturtic.tournament_api.dl.enums.MatchStatus;
import com.technofuturtic.tournament_api.dl.enums.RoundType;

import java.time.LocalDate;

public record UserMatchResponse(
        Integer id,
        Integer tournamentId,
        String tournamentName,
        RoundType roundType,
        Integer myParticipantId,
        BracketParticipantResponse participant1,
        BracketParticipantResponse participant2,
        Integer score1,
        Integer score2,
        BracketParticipantResponse winner,
        MatchStatus status,
        LocalDate scheduledDate
) {

    public static UserMatchResponse fromEntity(MatchEntity match, Integer userId) {
        TournamentEntity tournament = match.getRound().getPhase().getTournament();

        return new UserMatchResponse(
                match.getId(),
                tournament.getId(),
                tournament.getName(),
                match.getRound().getType(),
                myParticipantId(match, userId),
                BracketParticipantResponse.fromEntity(match.getParticipant1()),
                BracketParticipantResponse.fromEntity(match.getParticipant2()),
                match.getScore1(),
                match.getScore2(),
                BracketParticipantResponse.fromEntity(match.getWinner()),
                match.getStatus(),
                match.getScheduledDate()
        );
    }

    private static Integer myParticipantId(MatchEntity match, Integer userId) {
        if (isMine(match.getParticipant1(), userId)) {
            return match.getParticipant1().getId();
        }
        if (isMine(match.getParticipant2(), userId)) {
            return match.getParticipant2().getId();
        }
        return null;
    }

    private static boolean isMine(ParticipantEntity participant, Integer userId) {
        if (participant == null) {
            return false;
        }
        if (participant.getUser() != null) {
            return participant.getUser().getId().equals(userId);
        }
        return participant.getTeam() != null
                && participant.getTeam().getMembers().stream()
                .anyMatch(member -> member.getId().equals(userId));
    }
}