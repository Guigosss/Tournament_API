package com.technofuturtic.tournament_api.bll.services.impls;

import com.technofuturtic.tournament_api.api.models.UserContext;
import com.technofuturtic.tournament_api.dal.repositories.TournamentRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SecurityServiceImplTest {
    private final TournamentRepository tournaments = mock(TournamentRepository.class);
    private final SecurityServiceImpl service = new SecurityServiceImpl(tournaments);

    @AfterEach
    void clearAuthentication() {
        SecurityContextHolder.clearContext();
    }

    private void authenticate(Integer id, String role) {
        var user = new UserContext(id, "player" + id, role);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities()));
    }

    @Test
    void returnsTrueForAuthenticatedTournamentOrganizer() {
        authenticate(2, "user");
        when(tournaments.existsByIdAndOrganizer_Id(7, 2)).thenReturn(true);

        assertTrue(service.isTournamentOrganizer(7));
        verify(tournaments).existsByIdAndOrganizer_Id(7, 2);
    }

    @Test
    void returnsFalseForAnotherAuthenticatedPlayer() {
        authenticate(3, "user");
        when(tournaments.existsByIdAndOrganizer_Id(7, 3)).thenReturn(false);

        assertFalse(service.isTournamentOrganizer(7));
        verify(tournaments).existsByIdAndOrganizer_Id(7, 3);
    }

    @Test
    void administratorRoleDoesNotAutomaticallyMakeUserOrganizer() {
        authenticate(99, "admin");
        when(tournaments.existsByIdAndOrganizer_Id(7, 99)).thenReturn(false);

        assertFalse(service.isTournamentOrganizer(7));
        verify(tournaments).existsByIdAndOrganizer_Id(7, 99);
    }

    @Test
    void organizerOfOneTournamentIsNotOrganizerOfEveryTournament() {
        authenticate(2, "user");
        when(tournaments.existsByIdAndOrganizer_Id(7, 2)).thenReturn(true);
        when(tournaments.existsByIdAndOrganizer_Id(8, 2)).thenReturn(false);

        assertTrue(service.isTournamentOrganizer(7));
        assertFalse(service.isTournamentOrganizer(8));
        verify(tournaments).existsByIdAndOrganizer_Id(7, 2);
        verify(tournaments).existsByIdAndOrganizer_Id(8, 2);
    }
}
