package com.technofuturtic.tournament_api.dal.repositories;

import com.technofuturtic.tournament_api.dl.entities.MatchEntity;
import com.technofuturtic.tournament_api.dl.enums.MatchStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MatchRepository extends JpaRepository<MatchEntity, Integer> {

    List<MatchEntity> findByRoundId(Integer roundId);

    @Query("""
        SELECT m
        FROM MatchEntity m
        WHERE m.round.id = :roundId
          AND m.round.phase.id = :phaseId
          AND m.round.phase.tournament.id = :tournamentId
          AND m.status != 'BYE'
        ORDER BY m.id
        """)
    List<MatchEntity> findByTournamentPhaseAndRound(@Param("tournamentId") Integer tournamentId, @Param("phaseId") Integer phaseId, @Param("roundId") Integer roundId);

    Optional<MatchEntity> findByRoundIdAndOrderIndex(Integer roundId, Integer orderIndex);

    List<MatchEntity> findByRoundIdAndStatus(Integer roundId, MatchStatus status);
}
