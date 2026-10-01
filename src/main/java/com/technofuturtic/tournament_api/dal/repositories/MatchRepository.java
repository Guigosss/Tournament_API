package com.technofuturtic.tournament_api.dal.repositories;

import com.technofuturtic.tournament_api.dl.entities.MatchEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MatchRepository extends JpaRepository<MatchEntity, Integer> {
}
