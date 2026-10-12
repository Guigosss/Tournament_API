package com.technofuturtic.tournament_api.bll.services.impls;

import com.technofuturtic.tournament_api.api.models.team.responses.TeamInvitationResponse;
import com.technofuturtic.tournament_api.bll.exceptions.team.*;
import com.technofuturtic.tournament_api.bll.exceptions.user.UserNotFoundException;
import com.technofuturtic.tournament_api.bll.services.TeamInvitationService;
import com.technofuturtic.tournament_api.dal.repositories.*;
import com.technofuturtic.tournament_api.dl.entities.*;
import com.technofuturtic.tournament_api.dl.enums.InvitationStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.access.AccessDeniedException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TeamInvitationServiceImpl implements TeamInvitationService {
    private final TeamInvitationRepository invitationRepository;
    private final TeamRepository teamRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public TeamInvitationResponse invite(Integer teamId, Integer playerId, Integer senderId, boolean admin) {
        UserEntity player = userRepository.findByIdForUpdate(playerId)
                .orElseThrow(() -> new UserNotFoundException("Player not found"));
        if (player.isDeleted()) throw new UserNotFoundException("Player not found");
        TeamEntity team = teamRepository.findByIdForUpdate(teamId)
                .orElseThrow(() -> new TeamNotFoundException("Team not found"));
        if (!admin && (team.getCaptain() == null || !senderId.equals(team.getCaptain().getId()))) {
            throw new AccessDeniedException("Only the captain or an administrator can invite players");
        }
        checkMembershipAndCapacity(team, playerId);
        if (invitationRepository.existsByTeamIdAndPlayerIdAndStatus(teamId, playerId, InvitationStatus.PENDING)) {
            throw new TeamConflictException("invitation", "A pending invitation already exists");
        }
        TeamInvitationEntity invitation = new TeamInvitationEntity();
        invitation.setTeam(team);
        invitation.setPlayer(player);
        return TeamInvitationResponse.fromInvitation(invitationRepository.saveAndFlush(invitation));
    }

    @Override
    @Transactional(readOnly = true)
    public List<TeamInvitationResponse> findMyInvitations(Integer playerId) {
        List<TeamInvitationResponse> responses = new ArrayList<>();
        for (TeamInvitationEntity invitation : invitationRepository.findAllByPlayerIdOrderByCreatedAtDesc(playerId)) {
            responses.add(TeamInvitationResponse.fromInvitation(invitation));
        }
        return responses;
    }

    @Override
    @Transactional
    public TeamInvitationResponse accept(Integer invitationId, Integer playerId) {
        TeamInvitationRepository.InvitationTarget initial = invitationRepository.findTargetById(invitationId)
                .orElseThrow(TeamInvitationNotFoundException::new);
        if (!playerId.equals(initial.getPlayerId())) {
            throw new AccessDeniedException("Only the invited player can accept");
        }
        UserEntity player = userRepository.findByIdForUpdate(playerId)
                .orElseThrow(() -> new UserNotFoundException("Player not found"));
        if (player.isDeleted()) throw new UserNotFoundException("Player not found");
        // Always lock the team before the invitation to serialize membership changes.
        TeamEntity team = teamRepository.findByIdForUpdate(initial.getTeamId())
                .orElseThrow(() -> new TeamNotFoundException("Team not found"));
        TeamInvitationEntity invitation = invitationRepository.findByIdForUpdate(invitationId)
                .orElseThrow(TeamInvitationNotFoundException::new);
        if (invitation.getStatus() != InvitationStatus.PENDING) {
            throw new TeamConflictException("invitation", "Invitation is no longer pending");
        }
        checkMembershipAndCapacity(team, playerId);
        team.addMember(invitation.getPlayer());
        teamRepository.saveAndFlush(team);
        invitation.setStatus(InvitationStatus.ACCEPTED);
        invitation.setAcceptedAt(LocalDateTime.now());
        invitationRepository.saveAndFlush(invitation);
        return TeamInvitationResponse.fromInvitation(invitation);
    }

    private void checkMembershipAndCapacity(TeamEntity team, Integer playerId) {
        if (team.isArchived() || team.getCaptain() == null) {
            throw new TeamConflictException("team", "Cette équipe ne peut plus recruter de membres.");
        }
        for (UserEntity member : team.getMembers()) {
            if (playerId.equals(member.getId())) {
                throw new TeamConflictException("player", "Player is already a member of the team");
            }
        }
        if (team.getMembers().size() >= team.getTeamSize()) {
            throw new TeamConflictException("team", "Team is full");
        }
    }

    @Override
    @Transactional
    public TeamInvitationResponse reject(Integer invitationId, Integer playerId) {
        TeamInvitationRepository.InvitationTarget target = invitationRepository.findTargetById(invitationId)
                .orElseThrow(TeamInvitationNotFoundException::new);
        if (!playerId.equals(target.getPlayerId())) {
            throw new AccessDeniedException("Only the invited player can reject");
        }
        teamRepository.findByIdForUpdate(target.getTeamId())
                .orElseThrow(() -> new TeamNotFoundException("Team not found"));
        TeamInvitationEntity invitation = invitationRepository.findByIdForUpdate(invitationId)
                .orElseThrow(TeamInvitationNotFoundException::new);
        return closeInvitation(invitation, InvitationStatus.REJECTED);
    }

    @Override
    @Transactional
    public TeamInvitationResponse cancel(Integer invitationId, Integer userId, boolean admin) {
        TeamInvitationRepository.InvitationTarget target = invitationRepository.findTargetById(invitationId)
                .orElseThrow(TeamInvitationNotFoundException::new);
        TeamEntity team = teamRepository.findByIdForUpdate(target.getTeamId())
                .orElseThrow(() -> new TeamNotFoundException("Team not found"));
        if (!admin && (team.getCaptain() == null || !userId.equals(team.getCaptain().getId()))) {
            throw new AccessDeniedException("Only the captain or an administrator can cancel");
        }
        TeamInvitationEntity invitation = invitationRepository.findByIdForUpdate(invitationId)
                .orElseThrow(TeamInvitationNotFoundException::new);
        return closeInvitation(invitation, InvitationStatus.CANCELLED);
    }

    private TeamInvitationResponse closeInvitation(TeamInvitationEntity invitation, InvitationStatus status) {
        if (invitation.getStatus() != InvitationStatus.PENDING) {
            throw new TeamConflictException("invitation", "Invitation is no longer pending");
        }
        invitation.setStatus(status);
        invitationRepository.saveAndFlush(invitation);
        return TeamInvitationResponse.fromInvitation(invitation);
    }
}