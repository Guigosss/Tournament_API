package com.technofuturtic.tournament_api.api.models.team.requests;

import com.technofuturtic.tournament_api.dl.entities.TeamEntity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record TeamCreateRequest(
        @NotBlank(message = "Team name is required")
        @Size(max = 50, message = "Team name max 50 chars")
        String name
) {
    public TeamCreateRequest {
        if (name != null) {
            name = name.strip();
        }
    }
}