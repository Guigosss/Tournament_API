package com.technofuturtic.tournament_api.api.models.role.responses;

import com.technofuturtic.tournament_api.dl.entities.RoleEntity;

public record RoleResponse(
        Integer id,
        String name
) {

    public static RoleResponse fromRole(RoleEntity role) {
        return new RoleResponse(
                role.getId(),
                role.getName()
        );
    }
}
