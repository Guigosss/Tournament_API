package com.technofuturtic.tournament_api.dal.repositories;

import com.technofuturtic.tournament_api.dl.entities.TeamInvitationEntity;
import com.technofuturtic.tournament_api.dl.enums.InvitationStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface TeamInvitationRepository extends JpaRepository<TeamInvitationEntity, Integer> {
    interface InvitationTarget {
        Integer getTeamId();
        Integer getPlayerId();
    }

    @Query("select i.team.id as teamId, i.player.id as playerId from TeamInvitationEntity i where i.id = :id")
    Optional<InvitationTarget> findTargetById(Integer id);

    boolean existsByTeamIdAndPlayerIdAndStatus(Integer teamId, Integer playerId, InvitationStatus status);

    @EntityGraph(attributePaths = {"team", "player"})
    List<TeamInvitationEntity> findAllByPlayerIdOrderByCreatedAtDesc(Integer playerId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select i from TeamInvitationEntity i where i.id = :id")
    Optional<TeamInvitationEntity> findByIdForUpdate(Integer id);
}