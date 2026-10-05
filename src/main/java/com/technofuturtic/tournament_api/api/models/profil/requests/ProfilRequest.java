package com.technofuturtic.tournament_api.api.models.profil.requests;

import jakarta.validation.constraints.Size;
import com.technofuturtic.tournament_api.dl.entities.UserEntity;
import jakarta.validation.constraints.NotBlank;

public record ProfilRequest(
        @NotBlank(message = "Username is required")
        @Size(max = 50, message = "Username max 50 chars") String username
) {

    public UserEntity toUserEntity() {
        return new UserEntity(
                username
        );
    }
}