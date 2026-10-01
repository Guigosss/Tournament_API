package com.technofuturtic.tournament_api.api.models.user.requests;

import com.technofuturtic.tournament_api.dl.entities.UserEntity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;


public record RegisterRequest(
        @NotBlank(message = "Username is required")
        @Size(max = 50, message = "Username max 50 chars")
        String username,

        @NotBlank(message = "Email is required")
        String email,

        @NotBlank(message = "Password is required")
        String password
) {

    public UserEntity toUser() {
        return new UserEntity(
                username,
                email,
                password
        );
    }
}
