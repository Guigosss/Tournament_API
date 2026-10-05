package com.technofuturtic.tournament_api.api.models.team.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateTeamRequest(
        @NotBlank(message = "Team name is required")
        @Size(max = 50, message = "Team name max 50 chars")
        String name
) {
    public CreateTeamRequest {
        if (name != null) name = name.strip();
    }
}