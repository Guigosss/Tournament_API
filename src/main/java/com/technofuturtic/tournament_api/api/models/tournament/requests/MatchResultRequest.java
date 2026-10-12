package com.technofuturtic.tournament_api.api.models.tournament.requests;

public record MatchResultRequest(
        Integer score1,
        Integer score2,
        Integer winnerId
) {
}
