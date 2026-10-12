package com.technofuturtic.tournament_api.bll.services;

import com.technofuturtic.tournament_api.dl.entities.UserEntity;

public interface ProfilService {
    java.util.List<UserEntity> search(String username);

    UserEntity findById(Integer id);

    void update(Integer id, UserEntity user);

    void delete(Integer id);
}