package com.technofuturtic.tournament_api.bll.models.tournament.requests;

public record MatchResultRequest(
        Integer score1,
        Integer score2,
        Integer winnerId
) {
}
