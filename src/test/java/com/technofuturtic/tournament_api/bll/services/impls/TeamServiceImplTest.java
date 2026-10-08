package com.technofuturtic.tournament_api.bll.services.impls;

import com.technofuturtic.tournament_api.api.models.team.requests.TeamUpdateRequest;
import com.technofuturtic.tournament_api.bll.exceptions.team.*;
import com.technofuturtic.tournament_api.bll.exceptions.user.UserNotFoundException;
import com.technofuturtic.tournament_api.dal.repositories.*;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static com.technofuturtic.tournament_api.bll.services.impls.ServiceTestData.*;

class TeamServiceImplTest {
    private final TeamRepository teams = mock(TeamRepository.class);
    private final UserRepository users = mock(UserRepository.class);
    private final TeamServiceImpl service = new TeamServiceImpl(teams, users);

    @Test void creationAddsCreatorAsCaptainAndMember() {
        var creator = player(1);
        when(users.findByIdForUpdate(1)).thenReturn(Optional.of(creator));
        when(teams.saveAndFlush(any())).thenAnswer(call -> call.getArgument(0));
        var result = service.create(" Dragons ", 1);
        assertEquals("Dragons", result.getName());
        assertSame(creator, result.getCaptain());
        assertTrue(result.getMembers().contains(creator));
        assertFalse(result.isArchived());
    }

    @Test void creationRejectsDuplicateName() {
        when(users.findByIdForUpdate(1)).thenReturn(Optional.of(player(1)));
        when(teams.existsByName("Dragons")).thenReturn(true);
        assertThrows(TeamConflictException.class, () -> service.create("Dragons", 1));
        verify(teams, never()).saveAndFlush(any());
    }

    @Test void creationRejectsMissingOrDeletedCreator() {
        assertThrows(UserNotFoundException.class, () -> service.create("Dragons", 1));
        var deleted = player(1);
        deleted.setDeleted(true);
        when(users.findByIdForUpdate(1)).thenReturn(Optional.of(deleted));
        assertThrows(UserNotFoundException.class, () -> service.create("Dragons", 1));
        verify(teams, never()).saveAndFlush(any());
    }

    @Test void findReturnsTeamOrThrowsWhenMissing() {
        var team = team(7, player(1));
        when(teams.findWithMembersById(7)).thenReturn(Optional.of(team));
        assertSame(team, service.findById(7));
        assertThrows(TeamNotFoundException.class, () -> service.findById(99));
    }

    @Test void searchTrimsInputAndReturnsRepositoryResults() {
        var results = List.of(team(7, player(1)));
        when(teams.findTop20ByNameContainingIgnoreCaseOrderByNameAsc("dra")).thenReturn(results);
        assertSame(results, service.search(" dra "));
    }

    @Test void invalidSearchDoesNotQueryDatabase() {
        for (String query : new String[]{null, "", " ", "x".repeat(51)}) {
            assertEquals(400, assertThrows(ResponseStatusException.class, () -> service.search(query)).getStatusCode().value());
        }
        verifyNoInteractions(teams);
    }

    @Test void updateTransfersCaptaincyOnlyToExistingMember() {
        var team = team(7, player(1));
        var next = player(2);
        team.addMember(next);
        when(teams.findByIdForUpdate(7)).thenReturn(Optional.of(team));
        when(users.findById(2)).thenReturn(Optional.of(next));
        service.update(7, new TeamUpdateRequest(" Tigers ", 2));
        assertEquals("Tigers", team.getName());
        assertSame(next, team.getCaptain());
        verify(teams).save(team);
    }

    @Test void outsiderCannotBecomeCaptain() {
        var captain = player(1);
        var team = team(7, captain);
        when(teams.findByIdForUpdate(7)).thenReturn(Optional.of(team));
        when(users.findById(2)).thenReturn(Optional.of(player(2)));
        assertThrows(TeamConflictException.class, () -> service.update(7, new TeamUpdateRequest("Tigers", 2)));
        assertSame(captain, team.getCaptain());
        assertEquals("Team7", team.getName());
        verify(teams, never()).save(any());
    }

    @Test void updateRejectsDuplicateNameOrArchivedTeam() {
        var team = team(7, player(1));
        when(teams.findByIdForUpdate(7)).thenReturn(Optional.of(team));
        when(teams.existsByName("Tigers")).thenReturn(true);
        assertThrows(TeamConflictException.class, () -> service.update(7, new TeamUpdateRequest("Tigers", 1)));
        team.setArchived(true);
        assertThrows(TeamConflictException.class, () -> service.update(7, new TeamUpdateRequest("Team7", 1)));
        verify(teams, never()).save(any());
    }

    @Test void deleteArchivesTeamWithoutDeletingHistoryOrPlayers() {
        var team = team(7, player(1));
        team.setNumberOfWins(8);
        when(teams.findByIdForUpdate(7)).thenReturn(Optional.of(team));
        service.delete(7);
        assertTrue(team.isArchived());
        assertNull(team.getCaptain());
        assertTrue(team.getMembers().isEmpty());
        assertEquals(8, team.getNumberOfWins());
        assertEquals("Team7", team.getName());
        verify(teams).saveAndFlush(team);
        verify(teams, never()).delete(any());
        verifyNoInteractions(users);
    }

    @Test void updateAndDeleteRejectMissingTeam() {
        assertThrows(TeamNotFoundException.class, () -> service.update(7, new TeamUpdateRequest("Tigers", 1)));
        assertThrows(TeamNotFoundException.class, () -> service.delete(7));
    }
}
