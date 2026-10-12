package com.technofuturtic.tournament_api.api.models.user.requests;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.AssertTrue;
import java.nio.charset.StandardCharsets;
import com.technofuturtic.tournament_api.dl.entities.UserEntity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;


public record RegisterRequest(
        @NotBlank(message = "Username is required")
        @Size(max = 50, message = "Username max 50 chars")
        String username,

        @NotBlank(message = "Email is required")
        @Email(message = "Email is invalid")
        @Size(max = 254, message = "Email max 254 chars")
        String email,

        @NotBlank(message = "Password is required")
        @Size(min = 8, max = 72, message = "Password must contain between 8 and 72 characters")
        String password,

        @NotBlank(message = "Password confirmation is required")
        @Size(max = 72, message = "Password confirmation max 72 chars")
        String confirmPassword
) {

    public UserEntity toUser() {
        return new UserEntity(
                username,
                email,
                password
        );
    }

    @AssertTrue(message = "Password confirmation must match password")
    public boolean isPasswordConfirmationMatching() {
        return password != null && password.equals(confirmPassword);
    }

    @AssertTrue(message = "Password must not exceed 72 UTF-8 bytes")
    public boolean isPasswordWithinBcryptLimit() {
        return password == null || password.getBytes(StandardCharsets.UTF_8).length <= 72;
    }
}