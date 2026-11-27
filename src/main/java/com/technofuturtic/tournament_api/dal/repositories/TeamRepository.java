
package com.technofuturtic.tournament_api.dal.repositories;

import com.technofuturtic.tournament_api.dl.entities.TeamEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TeamRepository extends JpaRepository<TeamEntity, Integer> {

    @Query("""
        select distinct t.id
        from TeamEntity t
        left join t.members m
        where t.captain.id = :userId or m.id = :userId
        order by t.id
    """)
    List<Integer> findIdsByPlayerId(Integer userId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select t from TeamEntity t where t.id = :id")
    Optional<TeamEntity> findByIdForUpdate(Integer id);

    @EntityGraph(attributePaths = {"captain", "members"})
    @Query("select t from TeamEntity t where t.id = :id")
    Optional<TeamEntity> findWithMembersById(Integer id);

    @EntityGraph(attributePaths = {"captain", "members"})
    @Query("""
        select distinct t
        from TeamEntity t
        left join t.members m
        where (t.captain.id = :userId or m.id = :userId)
        and t.archived = false
        order by t.name asc
    """)
    List<TeamEntity> findAllByPlayerId(@Param("userId") Integer userId);

    @Query("""
        select t
        from TeamEntity t
        where t.archived = false
        and (
            :name is null
            or lower(t.name) like lower(concat('%', :name, '%'))
        )
        order by t.numberOfWins desc, t.name asc
    """)
    List<TeamEntity> searchAvailableTeams(
            @Param("name") String name,
            Pageable pageable
    );

    boolean existsByName(String name);

    @Query("""
        select count(t) > 0
        from TeamEntity t
        left join t.members m
        where t.captain.id = :userId or m.id = :userId
    """)
    boolean existsByPlayerId(Integer userId);

    boolean existsByIdAndCaptainId(Integer teamId, Integer captainId);
}
