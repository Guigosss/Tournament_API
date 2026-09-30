package com.technofuturtic.tournament_api.api.models.user.requests;

public record LoginRequest(
        String username,
        String password
) {
}
