package com.technofuturtic.tournament_api.bll.services.impls;

import com.technofuturtic.tournament_api.bll.services.NotificationService;
import com.technofuturtic.tournament_api.bll.exceptions.team.*;
import com.technofuturtic.tournament_api.bll.exceptions.user.UserNotFoundException;
import com.technofuturtic.tournament_api.dal.repositories.TeamRepository;
import com.technofuturtic.tournament_api.dl.entities.TeamEntity;
import com.technofuturtic.tournament_api.dl.enums.NotificationType;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static com.technofuturtic.tournament_api.bll.services.impls.ServiceTestData.*;

class TeamMemberServiceImplTest {
    private final TeamRepository teams = mock(TeamRepository.class);
    private final NotificationService notifications = mock(NotificationService.class);
    private final TeamMemberServiceImpl service = new TeamMemberServiceImpl(teams, notifications);

    private TeamEntity prepare() {
        var team = team(7, player(1));
        team.addMember(player(2));
        when(teams.findByIdForUpdate(7)).thenReturn(Optional.of(team));
        return team;
    }

    @Test void captainRemovalNotifiesExcludedPlayerAndPreservesOtherTeams() {
        var team = prepare();
        var member = player(2);
        var otherTeam = team(8, player(3));
        otherTeam.addMember(member);
        service.remove(7, 2, 1, false);
        assertEquals(1, team.getMembers().size());
        assertTrue(otherTeam.getMembers().contains(member));
        verify(notifications).notify(eq(member), eq(NotificationType.MEMBER_REMOVED), eq(7), contains("Team7"));
        verify(teams).saveAndFlush(team);
    }

    @Test void adminCanRemoveMember() {
        var team = prepare();
        service.remove(7, 2, 99, true);
        assertEquals(1, team.getMembers().size());
    }

    @Test void otherPlayersCannotRemoveMembers() {
        var team = prepare();
        assertThrows(AccessDeniedException.class, () -> service.remove(7, 2, 3, false));
        assertEquals(2, team.getMembers().size());
        verify(teams, never()).saveAndFlush(any());
        verifyNoInteractions(notifications);
    }

    @Test void leavingNotifiesCaptainAndRemovesOnlyLeavingPlayer() {
        var team = prepare();
        service.leave(7, 2);
        assertEquals(1, team.getMembers().size());
        verify(notifications).notify(eq(team.getCaptain()), eq(NotificationType.MEMBER_LEFT), eq(7), contains("player2"));
    }

    @Test void captainCannotLeaveOrBeRemovedWithoutTransfer() {
        var team = prepare();
        assertThrows(TeamConflictException.class, () -> service.leave(7, 1));
        assertThrows(TeamConflictException.class, () -> service.remove(7, 1, 99, true));
        assertEquals(2, team.getMembers().size());
        verifyNoInteractions(notifications);
    }

    @Test void absentMemberOrTeamIsRejected() {
        assertThrows(TeamNotFoundException.class, () -> service.leave(99, 2));
        prepare();
        assertThrows(UserNotFoundException.class, () -> service.leave(7, 99));
        assertThrows(UserNotFoundException.class, () -> service.remove(7, 99, 1, false));
        verifyNoInteractions(notifications);
    }
}
