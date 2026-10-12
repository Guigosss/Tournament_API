package com.technofuturtic.tournament_api.api.models.tournament.requests;

import jakarta.validation.constraints.NotNull;

public record TeamRegistrationRequest(@NotNull Integer teamId) {
}
