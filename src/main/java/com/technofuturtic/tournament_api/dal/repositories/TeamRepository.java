package com.technofuturtic.tournament_api.dal.repositories;

import com.technofuturtic.tournament_api.dl.entities.TeamEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface TeamRepository extends JpaRepository<TeamEntity, Integer> {
    boolean existsByName(String name);

    @Query("select count(t) > 0 from TeamEntity t left join t.members m "
            + "where t.captain.id = :userId or m.id = :userId")
    boolean existsByPlayerId(Integer userId);
}
