package com.technofuturtic.tournament_api.dal.repositories;

import com.technofuturtic.tournament_api.dl.entities.ParticipantEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ParticipantRepository extends JpaRepository<ParticipantEntity, Integer> {

    Optional<ParticipantEntity> findByUserIdAndTournamentId(Integer userId, Integer tournamentId);

    Optional<ParticipantEntity> findByTeamIdAndTournamentId(Integer teamId, Integer tournamentId);

}
