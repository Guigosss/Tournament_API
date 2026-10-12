package com.technofuturtic.tournament_api.bll.services.impls;

import com.technofuturtic.tournament_api.bll.exceptions.team.TeamConflictException;
import com.technofuturtic.tournament_api.bll.exceptions.user.UserNotFoundException;
import com.technofuturtic.tournament_api.dal.repositories.*;
import com.technofuturtic.tournament_api.dl.entities.*;
import com.technofuturtic.tournament_api.dl.enums.InvitationStatus;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static com.technofuturtic.tournament_api.bll.services.impls.ServiceTestData.*;

class TeamJoinRequestServiceImplTest {
    private final TeamJoinRequestRepository requests = mock(TeamJoinRequestRepository.class);
    private final TeamRepository teams = mock(TeamRepository.class);
    private final UserRepository users = mock(UserRepository.class);
    private final TeamJoinRequestServiceImpl service = new TeamJoinRequestServiceImpl(requests, teams, users);

    private TeamEntity prepareTeam() {
        var team = team(7, player(1));
        when(teams.findByIdForUpdate(7)).thenReturn(Optional.of(team));
        when(teams.findById(7)).thenReturn(Optional.of(team));
        when(users.findByIdForUpdate(2)).thenReturn(Optional.of(player(2)));
        return team;
    }

    private TeamJoinRequestEntity prepareRequest(TeamEntity team) {
        var request = new TeamJoinRequestEntity();
        request.setId(8);
        request.setTeam(team);
        request.setPlayer(player(2));
        var target = mock(TeamJoinRequestRepository.Target.class);
        when(target.getTeamId()).thenReturn(7);
        when(target.getPlayerId()).thenReturn(2);
        when(requests.findTargetById(8)).thenReturn(Optional.of(target));
        when(requests.findByIdForUpdate(8)).thenReturn(Optional.of(request));
        when(requests.saveAndFlush(any())).thenAnswer(call -> call.getArgument(0));
        return request;
    }

    @Test void submitCreatesPendingRequestWithoutAddingMember() {
        var team = prepareTeam();
        when(requests.saveAndFlush(any())).thenAnswer(call -> call.getArgument(0));
        var result = service.submit(7, 2);
        assertEquals(InvitationStatus.PENDING, result.status());
        assertEquals(2, result.playerId());
        assertEquals(1, team.getMembers().size());
        verify(teams, never()).saveAndFlush(any());
    }

    @Test void duplicatePendingRequestIsRejected() {
        prepareTeam();
        when(requests.existsByTeamIdAndPlayerIdAndStatus(7, 2, InvitationStatus.PENDING)).thenReturn(true);
        assertThrows(TeamConflictException.class, () -> service.submit(7, 2));
        verify(requests, never()).saveAndFlush(any());
    }

    @Test void captainAcceptsRequestAndAddsPlayer() {
        var team = prepareTeam();
        var request = prepareRequest(team);
        assertEquals(InvitationStatus.ACCEPTED, service.accept(8, 1, false).status());
        assertTrue(team.getMembers().contains(request.getPlayer()));
        assertEquals(1, team.getCaptain().getId());
        verify(teams).saveAndFlush(team);
    }

    @Test void adminCanAcceptButOtherPlayersCannot() {
        var team = prepareTeam();
        prepareRequest(team);
        assertThrows(AccessDeniedException.class, () -> service.accept(8, 3, false));
        assertEquals(1, team.getMembers().size());
        assertEquals(InvitationStatus.ACCEPTED, service.accept(8, 99, true).status());
    }

    @Test void captainCanRejectWithoutAddingPlayer() {
        var team = prepareTeam();
        prepareRequest(team);
        assertThrows(AccessDeniedException.class, () -> service.reject(8, 3, false));
        assertEquals(InvitationStatus.REJECTED, service.reject(8, 1, false).status());
        assertEquals(1, team.getMembers().size());
        verify(teams, never()).saveAndFlush(any());
    }

    @Test void onlyApplicantCanCancel() {
        prepareRequest(prepareTeam());
        assertThrows(AccessDeniedException.class, () -> service.cancel(8, 99));
        assertEquals(InvitationStatus.CANCELLED, service.cancel(8, 2).status());
    }

    @Test void closedRequestCannotChangeStatusAgain() {
        var request = prepareRequest(prepareTeam());
        for (InvitationStatus status : new InvitationStatus[]{InvitationStatus.ACCEPTED, InvitationStatus.REJECTED, InvitationStatus.CANCELLED}) {
            request.setStatus(status);
            assertThrows(TeamConflictException.class, () -> service.accept(8, 1, false));
            assertThrows(TeamConflictException.class, () -> service.reject(8, 1, false));
            assertThrows(TeamConflictException.class, () -> service.cancel(8, 2));
            assertEquals(status, request.getStatus());
        }
    }

    @Test void existingMemberCannotSubmitOrBeAddedTwice() {
        var team = prepareTeam();
        prepareRequest(team);
        team.addMember(player(2));
        assertThrows(TeamConflictException.class, () -> service.submit(7, 2));
        assertThrows(TeamConflictException.class, () -> service.accept(8, 1, false));
    }

    @Test void fullTeamCannotReceiveOrAcceptRequest() {
        var team = prepareTeam();
        var request = prepareRequest(team);
        for (int id = 3; id <= 6; id++) team.addMember(player(id));
        assertThrows(TeamConflictException.class, () -> service.submit(7, 2));
        assertThrows(TeamConflictException.class, () -> service.accept(8, 1, false));
        assertEquals(InvitationStatus.PENDING, request.getStatus());
    }

    @Test void deletedPlayerCannotSubmitOrBeAccepted() {
        prepareRequest(prepareTeam());
        var user = player(2);
        user.setDeleted(true);
        when(users.findByIdForUpdate(2)).thenReturn(Optional.of(user));
        assertThrows(UserNotFoundException.class, () -> service.submit(7, 2));
        assertThrows(UserNotFoundException.class, () -> service.accept(8, 1, false));
        verify(teams, never()).saveAndFlush(any());
    }

    @Test void archivedOrCaptainlessTeamCannotReceiveRequests() {
        var team = prepareTeam();
        team.setArchived(true);
        assertThrows(TeamConflictException.class, () -> service.submit(7, 2));
        team.setArchived(false);
        team.setCaptain(null);
        assertThrows(TeamConflictException.class, () -> service.submit(7, 2));
    }

    @Test void requestsAreVisibleOnlyToApplicantOrCaptainAndAdmin() {
        var request = prepareRequest(prepareTeam());
        when(requests.findAllByPlayerIdOrderByCreatedAtDescIdDesc(2)).thenReturn(List.of(request));
        when(requests.findAllByTeamIdOrderByCreatedAtDescIdDesc(7)).thenReturn(List.of(request));
        assertEquals(2, service.findMine(2).get(0).playerId());
        assertThrows(AccessDeniedException.class, () -> service.findForTeam(7, 3, false));
        assertEquals(1, service.findForTeam(7, 1, false).size());
        assertEquals(1, service.findForTeam(7, 99, true).size());
    }

    @Test void missingRequestReturns404() {
        assertEquals(404, assertThrows(ResponseStatusException.class, () -> service.accept(99, 1, false)).getStatusCode().value());
        assertThrows(ResponseStatusException.class, () -> service.reject(99, 1, false));
        assertThrows(ResponseStatusException.class, () -> service.cancel(99, 2));
    }
}
