package com.technofuturtic.tournament_api.dal.repositories;

import com.technofuturtic.tournament_api.dl.entities.PhaseEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PhaseRepository extends JpaRepository<PhaseEntity, Integer> {

    List<PhaseEntity> findByTournamentIdOrderByOrderIndexAsc(Integer tournamentId);
}
