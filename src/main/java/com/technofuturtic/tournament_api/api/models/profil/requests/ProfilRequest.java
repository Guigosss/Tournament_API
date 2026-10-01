package com.technofuturtic.tournament_api.api.models.profil.requests;

import com.technofuturtic.tournament_api.dl.entities.UserEntity;
import jakarta.validation.constraints.NotBlank;

public record ProfilRequest(
        @NotBlank(message = "Username is required") String username
) {

    public UserEntity toUserEntity() {
        return new UserEntity(
                username
        );
    }
}
