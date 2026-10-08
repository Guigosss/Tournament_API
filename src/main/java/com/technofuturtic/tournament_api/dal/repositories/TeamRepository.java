package com.technofuturtic.tournament_api.dal.repositories;

import com.technofuturtic.tournament_api.dl.entities.TeamEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface TeamRepository extends JpaRepository<TeamEntity, Integer> {
    java.util.List<TeamEntity> findTop20ByNameContainingIgnoreCaseOrderByNameAsc(String name);
    @Query("select distinct t.id from TeamEntity t left join t.members m where t.captain.id = :userId or m.id = :userId order by t.id")
    java.util.List<Integer> findIdsByPlayerId(Integer userId);
    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @Query("select t from TeamEntity t where t.id = :id")
    java.util.Optional<TeamEntity> findByIdForUpdate(Integer id);

    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = {"captain", "members"})
    @Query("select t from TeamEntity t where t.id = :id")
    java.util.Optional<TeamEntity> findWithMembersById(Integer id);

    boolean existsByName(String name);

    @Query("select count(t) > 0 from TeamEntity t left join t.members m "
            + "where t.captain.id = :userId or m.id = :userId")
    boolean existsByPlayerId(Integer userId);
    boolean existsByIdAndCaptainId(Integer teamId, Integer captainId);
}