package com.technofuturtic.tournament_api.bll.services.impls;

import com.technofuturtic.tournament_api.bll.exceptions.user.UserDeleteConflictException;
import com.technofuturtic.tournament_api.dal.repositories.UserRepository;
import jakarta.persistence.EntityManager;
import org.springframework.transaction.annotation.Transactional;
import com.technofuturtic.tournament_api.bll.exceptions.user.UserNotFoundException;
import com.technofuturtic.tournament_api.bll.exceptions.user.UserAlreadyExistException;
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
    private final UserRepository userRepository;
    private final EntityManager entityManager;

    @Override
    @Cacheable(cacheNames = "user", key = "#id")
    public UserEntity findById(Integer id) {

        return profilRepository.findById(id)
                .orElseThrow(() ->
                        new UserNotFoundException(
                                "User with id " + id + " does not exist"
                        )
                );
    }

    @Override
    @CacheEvict(cacheNames = "user", key = "#id")
    @Transactional
    public void update(Integer id, UserEntity user) {

        UserEntity existing = profilRepository.findById(id)
                .orElseThrow(() ->
                        new UserNotFoundException(
                                "User with id " + id + " does not exist"
                        )
                );

        if (userRepository.existsByUsernameAndIdNot(user.getUsername(), id)) {
            throw new UserAlreadyExistException();
        }
        existing.setUsername(user.getUsername());

        profilRepository.saveAndFlush(existing);
    }

    @Override
    @CacheEvict(cacheNames = "user", key = "#id")
    @Transactional
    public void delete(Integer id) {

        UserEntity existing = profilRepository.findById(id)
                .orElseThrow(() ->
                        new UserNotFoundException(
                                "User with id " + id + " does not exist"
                        )
                );

        // Preserve tournament history and require an explicit decision for captains and members.
        String[] references = {
                "select count(t) from TeamEntity t where t.captain.id = :id",
                "select count(t) from TeamEntity t join t.members m where m.id = :id",
                "select count(t) from TournamentEntity t where t.organizer.id = :id",
                "select count(r) from RegisterUserEntity r where r.user.id = :id",
                "select count(p) from ParticipantEntity p where p.user.id = :id",
                "select count(m) from MatchEntity m where m.winner.id = :id"
        };
        for (String query : references) {
            if (entityManager.createQuery(query, Long.class).setParameter("id", id).getSingleResult() > 0) {
                throw new UserDeleteConflictException();
            }
        }
        profilRepository.delete(existing);
        profilRepository.flush();
    }
}