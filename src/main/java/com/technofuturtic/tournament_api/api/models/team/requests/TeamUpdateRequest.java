package com.technofuturtic.tournament_api.api.models.team.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record TeamUpdateRequest(
        @NotBlank(message = "Team name is required")
        @Size(max = 50, message = "Team name max 50 chars")
        String name,

        @NotNull(message = "Captain id is required")
        Integer captainId
) {
    public TeamUpdateRequest {
        if (name != null) {
            name = name.strip();
        }
    }
}

