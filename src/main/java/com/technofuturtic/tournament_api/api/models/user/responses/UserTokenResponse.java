package com.technofuturtic.tournament_api.api.models.user.responses;

public record UserTokenResponse(
        UserResponse user,
        String accessToken,
        String refreshToken
) {
}
