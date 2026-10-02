package com.technofuturtic.tournament_api.dal.repositories;

import com.technofuturtic.tournament_api.dl.entities.RoundEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoundRepository extends JpaRepository<RoundEntity, Integer> {

    List<RoundEntity> findByPhaseIdOrderByOrderIndex(Integer phaseId);

    @Query("""
        SELECT r
        FROM RoundEntity r
        WHERE r.phase.id = :phaseId
          AND r.phase.tournament.id = :tournamentId
        ORDER BY r.orderIndex
        """)
    List<RoundEntity> findByTournamentAndPhase(@Param("tournamentId") Integer tournamentId, @Param("phaseId") Integer phaseId);
}
