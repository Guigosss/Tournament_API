package com.technofuturtic.tournament_api.bll.services;

import com.technofuturtic.tournament_api.dl.entities.UserEntity;
import org.springframework.security.core.userdetails.UserDetailsService;

public interface AuthService extends UserDetailsService {

    UserEntity register(UserEntity user);
    UserEntity login(String username, String password);
    UserEntity findById(Integer id);
}
