package com.technofuturtic.tournament_api.dal.repositories;

import com.technofuturtic.tournament_api.dl.entities.RegisterUserEntity;
import com.technofuturtic.tournament_api.dl.enums.RegistrationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RegisterUserRepository extends JpaRepository<RegisterUserEntity, Integer> {

    boolean existsByUserIdAndTournamentId(Integer userId, Integer tournamentId);

    long countByTournamentId(Integer tournamentId);

    List<RegisterUserEntity> findByTournamentId(Integer tournamentId);

    long countByTournamentIdAndStatusNot(Integer tournamentId, RegistrationStatus status);
}
