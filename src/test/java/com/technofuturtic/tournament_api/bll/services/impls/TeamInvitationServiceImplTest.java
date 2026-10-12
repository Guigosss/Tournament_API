package com.technofuturtic.tournament_api.bll.services.impls;

import com.technofuturtic.tournament_api.bll.exceptions.team.*;
import com.technofuturtic.tournament_api.bll.exceptions.user.UserNotFoundException;
import com.technofuturtic.tournament_api.dal.repositories.*;
import com.technofuturtic.tournament_api.dl.entities.*;
import com.technofuturtic.tournament_api.dl.enums.InvitationStatus;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static com.technofuturtic.tournament_api.bll.services.impls.ServiceTestData.*;

class TeamInvitationServiceImplTest {
    private final TeamInvitationRepository invitations = mock(TeamInvitationRepository.class);
    private final TeamRepository teams = mock(TeamRepository.class);
    private final UserRepository users = mock(UserRepository.class);
    private final TeamInvitationServiceImpl service = new TeamInvitationServiceImpl(invitations, teams, users);

    private TeamEntity prepareTeam() {
        var team = team(7, player(1));
        when(teams.findByIdForUpdate(7)).thenReturn(Optional.of(team));
        when(users.findByIdForUpdate(2)).thenReturn(Optional.of(player(2)));
        return team;
    }

    private TeamInvitationEntity prepareInvitation(TeamEntity team) {
        var invitation = new TeamInvitationEntity();
        invitation.setId(8);
        invitation.setTeam(team);
        invitation.setPlayer(player(2));
        var target = mock(TeamInvitationRepository.InvitationTarget.class);
        when(target.getTeamId()).thenReturn(7);
        when(target.getPlayerId()).thenReturn(2);
        when(invitations.findTargetById(8)).thenReturn(Optional.of(target));
        when(invitations.findByIdForUpdate(8)).thenReturn(Optional.of(invitation));
        return invitation;
    }

    @Test void captainCanInviteWithoutImmediatelyAddingMember() {
        var team = prepareTeam();
        when(invitations.saveAndFlush(any())).thenAnswer(call -> call.getArgument(0));
        var result = service.invite(7, 2, 1, false);
        assertEquals(InvitationStatus.PENDING, result.status());
        assertEquals(2, result.playerId());
        assertEquals(1, team.getMembers().size());
    }

    @Test void adminCanInviteButOtherPlayersCannot() {
        prepareTeam();
        when(invitations.saveAndFlush(any())).thenAnswer(call -> call.getArgument(0));
        assertThrows(AccessDeniedException.class, () -> service.invite(7, 2, 3, false));
        assertEquals(InvitationStatus.PENDING, service.invite(7, 2, 99, true).status());
    }

    @Test void duplicatePendingInvitationIsRejected() {
        prepareTeam();
        when(invitations.existsByTeamIdAndPlayerIdAndStatus(7, 2, InvitationStatus.PENDING)).thenReturn(true);
        assertThrows(TeamConflictException.class, () -> service.invite(7, 2, 1, false));
        verify(invitations, never()).saveAndFlush(any());
    }

    @Test void existingMemberCannotBeInvitedOrAddedAgain() {
        var team = prepareTeam();
        prepareInvitation(team);
        team.addMember(player(2));
        assertThrows(TeamConflictException.class, () -> service.invite(7, 2, 1, false));
        assertThrows(TeamConflictException.class, () -> service.accept(8, 2));
        verify(teams, never()).saveAndFlush(any());
    }

    @Test void fullTeamRejectsInvitationAndAcceptance() {
        var team = prepareTeam();
        var invitation = prepareInvitation(team);
        for (int id = 3; id <= 6; id++) team.addMember(player(id));
        assertThrows(TeamConflictException.class, () -> service.invite(7, 2, 1, false));
        assertThrows(TeamConflictException.class, () -> service.accept(8, 2));
        assertEquals(InvitationStatus.PENDING, invitation.getStatus());
    }

    @Test void acceptanceAddsPlayerKeepsCaptainAndRecordsDate() {
        var team = prepareTeam();
        var invitation = prepareInvitation(team);
        var result = service.accept(8, 2);
        assertEquals(InvitationStatus.ACCEPTED, result.status());
        assertTrue(team.getMembers().contains(invitation.getPlayer()));
        assertEquals(1, team.getCaptain().getId());
        assertNotNull(invitation.getAcceptedAt());
        verify(teams).saveAndFlush(team);
        verify(invitations).saveAndFlush(invitation);
    }

    @Test void onlyRecipientCanAcceptOrReject() {
        prepareInvitation(prepareTeam());
        assertThrows(AccessDeniedException.class, () -> service.accept(8, 99));
        assertThrows(AccessDeniedException.class, () -> service.reject(8, 99));
        verify(invitations, never()).saveAndFlush(any());
    }

    @Test void recipientCanRejectAndCaptainOrAdminCanCancel() {
        var invitation = prepareInvitation(prepareTeam());
        assertEquals(InvitationStatus.REJECTED, service.reject(8, 2).status());
        invitation.setStatus(InvitationStatus.PENDING);
        assertThrows(AccessDeniedException.class, () -> service.cancel(8, 3, false));
        assertEquals(InvitationStatus.CANCELLED, service.cancel(8, 1, false).status());
        invitation.setStatus(InvitationStatus.PENDING);
        assertEquals(InvitationStatus.CANCELLED, service.cancel(8, 99, true).status());
        verify(teams, never()).saveAndFlush(any());
    }

    @Test void closedInvitationCannotChangeStatusAgain() {
        var invitation = prepareInvitation(prepareTeam());
        for (InvitationStatus status : new InvitationStatus[]{InvitationStatus.ACCEPTED, InvitationStatus.REJECTED, InvitationStatus.CANCELLED}) {
            invitation.setStatus(status);
            assertThrows(TeamConflictException.class, () -> service.accept(8, 2));
            assertThrows(TeamConflictException.class, () -> service.reject(8, 2));
            assertThrows(TeamConflictException.class, () -> service.cancel(8, 1, false));
            assertEquals(status, invitation.getStatus());
        }
    }

    @Test void deletedPlayerCannotBeInvitedOrAccept() {
        prepareInvitation(prepareTeam());
        var deleted = player(2);
        deleted.setDeleted(true);
        when(users.findByIdForUpdate(2)).thenReturn(Optional.of(deleted));
        assertThrows(UserNotFoundException.class, () -> service.invite(7, 2, 1, false));
        assertThrows(UserNotFoundException.class, () -> service.accept(8, 2));
    }

    @Test void archivedTeamCannotInviteOrAccept() {
        var team = prepareTeam();
        prepareInvitation(team);
        team.setArchived(true);
        assertThrows(TeamConflictException.class, () -> service.invite(7, 2, 1, false));
        assertThrows(TeamConflictException.class, () -> service.accept(8, 2));
    }

    @Test void missingInvitationReturnsNotFound() {
        assertThrows(TeamInvitationNotFoundException.class, () -> service.accept(99, 2));
        assertThrows(TeamInvitationNotFoundException.class, () -> service.reject(99, 2));
        assertThrows(TeamInvitationNotFoundException.class, () -> service.cancel(99, 1, false));
    }

    @Test void listUsesOnlyRecipientAndPreservesStatusHistory() {
        var invitation = prepareInvitation(prepareTeam());
        invitation.setStatus(InvitationStatus.REJECTED);
        when(invitations.findAllByPlayerIdOrderByCreatedAtDesc(2)).thenReturn(List.of(invitation));
        var result = service.findMyInvitations(2);
        assertEquals(1, result.size());
        assertEquals(InvitationStatus.REJECTED, result.get(0).status());
        verify(invitations).findAllByPlayerIdOrderByCreatedAtDesc(2);
    }
}
