package com.technofuturtic.tournament_api.api.models.tournament.responses;

import com.technofuturtic.tournament_api.dl.enums.RegistrationStatus;

public record RegistrationCheckResponse(
        boolean registered,
        RegistrationStatus status
) {
}