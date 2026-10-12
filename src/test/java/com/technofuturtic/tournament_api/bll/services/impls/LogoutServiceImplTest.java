package com.technofuturtic.tournament_api.bll.services.impls;

import com.technofuturtic.tournament_api.dal.repositories.UserRepository;
import com.technofuturtic.tournament_api.bll.exceptions.user.UserNotFoundException;
import org.junit.jupiter.api.Test;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static com.technofuturtic.tournament_api.bll.services.impls.ServiceTestData.*;

class LogoutServiceImplTest {
    private final UserRepository users = mock(UserRepository.class);
    private final LogoutServiceImpl service = new LogoutServiceImpl(users);

    @Test void logoutIncrementsExistingTokenVersionAndKeepsAccountActive() {
        var user = player(1);
        user.setTokenVersion(4);
        when(users.findByIdForUpdate(1)).thenReturn(Optional.of(user));
        service.logout(1);
        assertEquals(5, user.getTokenVersion());
        assertFalse(user.isDeleted());
        verify(users).saveAndFlush(user);
    }

    @Test void missingAccountCannotLogout() {
        assertThrows(UserNotFoundException.class, () -> service.logout(1));
        verify(users, never()).saveAndFlush(any());
    }
}
