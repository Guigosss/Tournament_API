package com.technofuturtic.tournament_api.api.models.team.responses;

public record TeamSearchResponse(
        Integer id,
        String name,
        Integer numberOfWins,
        Integer teamSize,
        boolean archived
) {}
