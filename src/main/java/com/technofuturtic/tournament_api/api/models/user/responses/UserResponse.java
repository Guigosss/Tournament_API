package com.technofuturtic.tournament_api.api.models.user.responses;

import com.technofuturtic.tournament_api.api.models.role.responses.RoleResponse;
import com.technofuturtic.tournament_api.dl.entities.UserEntity;

public record UserResponse(
        Integer id,
        String username,
        RoleResponse role
) {

    public static UserResponse fromUser(UserEntity user) {
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                RoleResponse.fromRole(user.getRole())
        );
    }
}
