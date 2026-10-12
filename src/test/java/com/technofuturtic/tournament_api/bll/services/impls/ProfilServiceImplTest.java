package com.technofuturtic.tournament_api.bll.services.impls;

import com.technofuturtic.tournament_api.bll.exceptions.user.*;
import com.technofuturtic.tournament_api.bll.exceptions.team.TeamConflictException;
import com.technofuturtic.tournament_api.bll.services.NotificationService;
import com.technofuturtic.tournament_api.dal.repositories.*;
import com.technofuturtic.tournament_api.dl.entities.UserEntity;
import com.technofuturtic.tournament_api.dl.enums.NotificationType;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static com.technofuturtic.tournament_api.bll.services.impls.ServiceTestData.*;

class ProfilServiceImplTest {
    private final ProfilRepository profiles = mock(ProfilRepository.class);
    private final UserRepository users = mock(UserRepository.class);
    private final TeamRepository teams = mock(TeamRepository.class);
    private final NotificationService notifications = mock(NotificationService.class);
    private final ProfilServiceImpl service = new ProfilServiceImpl(profiles, users, teams, notifications);

    @Test void findReturnsActiveProfileAndRejectsMissingOrDeletedProfile() {
        var user = player(1);
        when(profiles.findById(1)).thenReturn(Optional.of(user));
        assertSame(user, service.findById(1));
        assertThrows(UserNotFoundException.class, () -> service.findById(99));
        user.setDeleted(true);
        assertThrows(UserNotFoundException.class, () -> service.findById(1));
    }

    @Test void searchTrimsInputAndRejectsInvalidQueries() {
        var result = List.of(player(1));
        when(users.findTop20ByDeletedFalseAndUsernameContainingIgnoreCaseOrderByUsernameAsc("val")).thenReturn(result);
        assertSame(result, service.search(" val "));
        for (String query : new String[]{null, " ", "x".repeat(51)}) {
            assertThrows(ResponseStatusException.class, () -> service.search(query));
        }
        verify(users).findTop20ByDeletedFalseAndUsernameContainingIgnoreCaseOrderByUsernameAsc("val");
    }

    @Test void updateChangesOnlyUsernameAndKeepsCredentials() {
        var user = player(1);
        when(profiles.findById(1)).thenReturn(Optional.of(user));
        service.update(1, new UserEntity("NewName"));
        assertEquals("NewName", user.getUsername());
        assertEquals("p1@test.be", user.getEmail());
        assertEquals("hash", user.getPassword());
        verify(profiles).saveAndFlush(user);
    }

    @Test void updateRejectsDuplicateUsernameWithoutModifyingProfile() {
        var user = player(1);
        when(profiles.findById(1)).thenReturn(Optional.of(user));
        when(users.existsByUsernameAndIdNot("Taken", 1)).thenReturn(true);
        assertThrows(UserAlreadyExistException.class, () -> service.update(1, new UserEntity("Taken")));
        assertEquals("player1", user.getUsername());
        verify(profiles, never()).saveAndFlush(any());
    }

    @Test void deletionRemovesPlayerFromAllTeamsAndNotifiesEachCaptain() {
        var user = player(2);
        var captain = player(1);
        var first = team(7, captain);
        var second = team(8, captain);
        first.addMember(user);
        second.addMember(user);
        when(users.findByIdForUpdate(2)).thenReturn(Optional.of(user));
        when(teams.findIdsByPlayerId(2)).thenReturn(List.of(7, 8));
        when(teams.findByIdForUpdate(7)).thenReturn(Optional.of(first));
        when(teams.findByIdForUpdate(8)).thenReturn(Optional.of(second));
        service.delete(2);
        assertEquals(1, first.getMembers().size());
        assertEquals(1, second.getMembers().size());
        assertTrue(user.isDeleted());
        assertEquals(2, user.getId());
        assertNotEquals("player2", user.getUsername());
        assertNotEquals("p2@test.be", user.getEmail());
        assertEquals("!", user.getPassword());
        verify(notifications).notify(eq(captain), eq(NotificationType.MEMBER_LEFT), eq(7), contains("player2"));
        verify(notifications).notify(eq(captain), eq(NotificationType.MEMBER_LEFT), eq(8), contains("player2"));
        verify(users).saveAndFlush(user);
        verify(users, never()).delete(any());
    }

    @Test void captainMustTransferRoleBeforeAnyTeamIsModified() {
        var user = player(2);
        var first = team(7, player(1));
        first.addMember(user);
        var second = team(8, user);
        second.addMember(player(3));
        when(users.findByIdForUpdate(2)).thenReturn(Optional.of(user));
        when(teams.findIdsByPlayerId(2)).thenReturn(List.of(7, 8));
        when(teams.findByIdForUpdate(7)).thenReturn(Optional.of(first));
        when(teams.findByIdForUpdate(8)).thenReturn(Optional.of(second));
        assertThrows(TeamConflictException.class, () -> service.delete(2));
        assertEquals(2, first.getMembers().size());
        assertSame(user, second.getCaptain());
        assertFalse(user.isDeleted());
        verify(teams, never()).saveAndFlush(any());
        verify(users, never()).saveAndFlush(any());
        verifyNoInteractions(notifications);
    }

    @Test void soleCaptainCanDeleteAccountAndEmptyTeamIsPreserved() {
        var user = player(1);
        var team = team(7, user);
        when(users.findByIdForUpdate(1)).thenReturn(Optional.of(user));
        when(teams.findIdsByPlayerId(1)).thenReturn(List.of(7));
        when(teams.findByIdForUpdate(7)).thenReturn(Optional.of(team));
        service.delete(1);
        assertTrue(user.isDeleted());
        assertNull(team.getCaptain());
        assertTrue(team.getMembers().isEmpty());
        verify(teams, never()).delete(any());
        verifyNoInteractions(notifications);
    }

    @Test void missingOrDeletedAccountCannotBeUpdatedOrDeleted() {
        assertThrows(UserNotFoundException.class, () -> service.update(1, new UserEntity("New")));
        assertThrows(UserNotFoundException.class, () -> service.delete(1));
        var user = player(1);
        user.setDeleted(true);
        when(users.findByIdForUpdate(1)).thenReturn(Optional.of(user));
        when(profiles.findById(1)).thenReturn(Optional.of(user));
        assertThrows(UserNotFoundException.class, () -> service.delete(1));
        assertThrows(UserNotFoundException.class, () -> service.update(1, new UserEntity("New")));
    }
}
