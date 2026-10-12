package com.technofuturtic.tournament_api.dal.repositories;

import com.technofuturtic.tournament_api.dl.entities.TeamJoinRequestEntity;
import com.technofuturtic.tournament_api.dl.enums.InvitationStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import java.util.List;
import java.util.Optional;

public interface TeamJoinRequestRepository extends JpaRepository<TeamJoinRequestEntity, Integer> {
    interface Target {
        Integer getTeamId();
        Integer getPlayerId();
    }

    @Query("select r.team.id as teamId, r.player.id as playerId from TeamJoinRequestEntity r where r.id = :id")
    Optional<Target> findTargetById(Integer id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from TeamJoinRequestEntity r where r.id = :id")
    Optional<TeamJoinRequestEntity> findByIdForUpdate(Integer id);

    boolean existsByTeamIdAndPlayerIdAndStatus(Integer teamId, Integer playerId, InvitationStatus status);

    @EntityGraph(attributePaths = {"team", "player"})
    List<TeamJoinRequestEntity> findAllByPlayerIdOrderByCreatedAtDescIdDesc(Integer playerId);

    @EntityGraph(attributePaths = {"team", "player"})
    List<TeamJoinRequestEntity> findAllByTeamIdOrderByCreatedAtDescIdDesc(Integer teamId);
}