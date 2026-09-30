package com.technofuturtic.tournament_api.dal.repositories;

import com.technofuturtic.tournament_api.dl.entities.RegisterTeamEntity;
import com.technofuturtic.tournament_api.dl.enums.RegistrationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RegisterTeamRepository extends JpaRepository<RegisterTeamEntity, Integer> {

    boolean existsByTeamIdAndTournamentId(Integer teamId, Integer tournamentId);

    long countByTournamentId(Integer tournamentId);

    List<RegisterTeamEntity> findByTournamentId(Integer tournamentId);

    long countByTournamentIdAndStatusNot(Integer tournamentId, RegistrationStatus status);
}
