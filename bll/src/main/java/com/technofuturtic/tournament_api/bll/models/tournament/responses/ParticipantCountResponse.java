package com.technofuturtic.tournament_api.bll.models.tournament.responses;

public record ParticipantCountResponse(
        Integer tournamentId,
        long current,
        Integer max,
        long remaining
) {
}