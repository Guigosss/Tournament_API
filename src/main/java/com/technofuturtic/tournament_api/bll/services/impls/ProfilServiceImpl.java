package com.technofuturtic.tournament_api.bll.services.impls;

import com.technofuturtic.tournament_api.dal.repositories.UserRepository;
import com.technofuturtic.tournament_api.dal.repositories.TeamRepository;
import com.technofuturtic.tournament_api.bll.services.NotificationService;
import com.technofuturtic.tournament_api.dl.enums.NotificationType;
import java.util.UUID;
import java.util.ArrayList;
import java.util.List;
import com.technofuturtic.tournament_api.dl.entities.TeamEntity;
import com.technofuturtic.tournament_api.bll.exceptions.team.TeamConflictException;
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
    private final TeamRepository teamRepository;
    private final NotificationService notificationService;

    @Override
    @Transactional(readOnly = true)
    public List<UserEntity> search(String username) {
        if (username == null || username.isBlank() || username.strip().length() > 50) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.BAD_REQUEST, "Le pseudo recherché doit contenir entre 1 et 50 caractères.");
        }
        return userRepository.findTop20ByDeletedFalseAndUsernameContainingIgnoreCaseOrderByUsernameAsc(username.strip());
    }

    @Override
    @Cacheable(cacheNames = "user", key = "#id")
    public UserEntity findById(Integer id) {

        return profilRepository.findById(id)
                .filter(user -> !user.isDeleted())
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
                .filter(found -> !found.isDeleted())
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

        UserEntity existing = userRepository.findByIdForUpdate(id)
                .filter(found -> !found.isDeleted())
                .orElseThrow(() ->
                        new UserNotFoundException(
                                "User with id " + id + " does not exist"
                        )
                );

        String oldUsername = existing.getUsername();
        List<TeamEntity> playerTeams = new ArrayList<>();
        for (Integer teamId : teamRepository.findIdsByPlayerId(id)) {
            var team = teamRepository.findByIdForUpdate(teamId).orElse(null);
            if (team == null) continue;
            if (team.getCaptain() != null && id.equals(team.getCaptain().getId())) {
                for (UserEntity member : team.getMembers()) {
                    if (!id.equals(member.getId())) {
                        throw new TeamConflictException("captainId",
                                "Vous devez choisir un autre membre comme capitaine de l'équipe "
                                        + team.getName() + " avant de supprimer votre compte.");
                    }
                }
            }
            playerTeams.add(team);
        }

        // Vérifier toutes les équipes avant de commencer les retraits.
        for (TeamEntity team : playerTeams) {
            UserEntity memberToRemove = null;
            for (UserEntity member : team.getMembers()) {
                if (id.equals(member.getId())) {
                    memberToRemove = member;
                    break;
                }
            }
            if (memberToRemove != null) team.getMembers().remove(memberToRemove);
            if (team.getCaptain() != null && id.equals(team.getCaptain().getId())) {
                team.setCaptain(null);
            }
            teamRepository.saveAndFlush(team);
            if (team.getCaptain() != null) {
                notificationService.notify(team.getCaptain(),
                        NotificationType.MEMBER_LEFT, team.getId(),
                        oldUsername + " a supprimé son compte et quitté votre équipe " + team.getName() + ".");
            }
        }
        String anonymousId = UUID.randomUUID().toString();
        existing.setUsername("deleted_" + anonymousId);
        existing.setEmail(anonymousId + "@deleted.invalid");
        existing.setPassword("!");
        existing.setDeleted(true);
        userRepository.saveAndFlush(existing);
    }
}