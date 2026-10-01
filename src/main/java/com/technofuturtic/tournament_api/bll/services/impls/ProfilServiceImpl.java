package com.technofuturtic.tournament_api.bll.services.impls;

import com.technofuturtic.tournament_api.bll.services.ProfilService;
import com.technofuturtic.tournament_api.dal.repositories.ProfilRepository;
import com.technofuturtic.tournament_api.dl.entities.UserEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProfilServiceImpl implements ProfilService {

    private final ProfilRepository profilRepository;

    @Override
    @Cacheable(cacheNames = "user", key = "#id")
    public UserEntity findById(Integer id) {

        return profilRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "User with id " + id + " does not exist"
                        )
                );
    }

    @Override
    @CacheEvict(cacheNames = "user", key = "#id")
    public void update(Integer id, UserEntity user) {

        UserEntity existing = profilRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "User with id " + id + " does not exist"
                        )
                );

        existing.setUsername(user.getUsername());

        profilRepository.save(existing);
    }

    @Override
    @CacheEvict(cacheNames = "user", key = "#id")
    public void delete(Integer id) {

        UserEntity existing = profilRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "User with id " + id + " does not exist"
                        )
                );

        profilRepository.delete(existing);
    }
}
