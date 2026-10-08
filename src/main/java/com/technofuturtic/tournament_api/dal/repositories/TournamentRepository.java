package com.technofuturtic.tournament_api.dal.repositories;

import com.technofuturtic.tournament_api.dl.entities.TournamentEntity;
import com.technofuturtic.tournament_api.dl.enums.TournamentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TournamentRepository extends JpaRepository<TournamentEntity, Integer> {

    List<TournamentEntity> findByStatus(TournamentStatus status);

    boolean existsByIdAndOrganizer_Id(Integer tournamentId, Integer organizerId);
}