package com.technofuturtic.tournament_api.bll.services.impls;

import com.technofuturtic.tournament_api.api.models.UserContext;
import com.technofuturtic.tournament_api.dal.repositories.TeamRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CaptainServiceImplTest {
    private final TeamRepository teams = mock(TeamRepository.class);
    private final CaptainServiceImpl service = new CaptainServiceImpl(teams);

    @AfterEach void clearAuthentication() { SecurityContextHolder.clearContext(); }

    @Test void returnsTrueForAuthenticatedCaptain() {
        var user = new UserContext(2, "Val", "user");
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities()));
        when(teams.existsByIdAndCaptainId(7, 2)).thenReturn(true);
        assertTrue(service.isTeamCaptain(7));
        verify(teams).existsByIdAndCaptainId(7, 2);
    }

    @Test void returnsFalseForAuthenticatedPlayerWhoIsNotCaptain() {
        var user = new UserContext(3, "Other", "user");
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities()));
        assertFalse(service.isTeamCaptain(7));
        verify(teams).existsByIdAndCaptainId(7, 3);
    }
}
