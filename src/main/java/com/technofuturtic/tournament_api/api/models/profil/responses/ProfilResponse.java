package com.technofuturtic.tournament_api.api.models.profil.responses;

import com.technofuturtic.tournament_api.dl.entities.UserEntity;

public record ProfilResponse(
        Integer id,
        String name
) {

    public static ProfilResponse fromUserEntity(UserEntity user) {
        return new ProfilResponse(
                user.getId(),
                user.getUsername()
        );
    }
}
