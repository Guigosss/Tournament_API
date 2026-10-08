package com.technofuturtic.tournament_api.api.models.team.requests;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record TeamInvitationRequest(
        @NotNull(message = "Player id is required")
        @Positive(message = "Player id must be positive") Integer playerId
) {}