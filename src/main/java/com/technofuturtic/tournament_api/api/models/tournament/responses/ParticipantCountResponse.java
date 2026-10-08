package com.technofuturtic.tournament_api.api.models.tournament.responses;

public record ParticipantCountResponse(
        Integer tournamentId,
        long current,
        Integer max,
        long remaining
) {
}