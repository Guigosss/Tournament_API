package com.technofuturtic.tournament_api.bll.services.impls;

import com.technofuturtic.tournament_api.bll.exceptions.user.*;
import com.technofuturtic.tournament_api.bll.exceptions.role.RoleNotFoundException;
import com.technofuturtic.tournament_api.dal.repositories.*;
import com.technofuturtic.tournament_api.dl.entities.*;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static com.technofuturtic.tournament_api.bll.services.impls.ServiceTestData.*;

class AuthServiceImplTest {
    private final UserRepository users = mock(UserRepository.class);
    private final RoleRepository roles = mock(RoleRepository.class);
    private final PasswordEncoder encoder = mock(PasswordEncoder.class);
    private final AuthServiceImpl service = new AuthServiceImpl(users, roles, encoder);

    @Test void registerHashesPasswordAndAssignsUserRole() {
        UserEntity user = player(1);
        RoleEntity role = new RoleEntity("user");
        when(encoder.encode("hash")).thenReturn("encoded");
        when(roles.findByName("user")).thenReturn(Optional.of(role));
        when(users.saveAndFlush(user)).thenReturn(user);
        assertSame(user, service.register(user));
        assertEquals("encoded", user.getPassword());
        assertSame(role, user.getRole());
        verify(users).saveAndFlush(user);
    }

    @Test void duplicateUsernameIsRejectedWithoutSaving() {
        when(users.existsByUsername("player1")).thenReturn(true);
        assertThrows(UserAlreadyExistException.class, () -> service.register(player(1)));
        verify(users, never()).saveAndFlush(any());
        verifyNoInteractions(encoder, roles);
    }

    @Test void duplicateEmailIsRejectedWithoutSaving() {
        when(users.existsByEmail("p1@test.be")).thenReturn(true);
        assertThrows(UserAlreadyExistException.class, () -> service.register(player(1)));
        verify(users, never()).saveAndFlush(any());
    }

    @Test void missingDefaultRoleIsRejected() {
        assertThrows(RoleNotFoundException.class, () -> service.register(player(1)));
        verify(users, never()).saveAndFlush(any());
    }

    @Test void loginReturnsUserWithValidPassword() {
        UserEntity user = player(1);
        when(users.findByUsername("player1")).thenReturn(Optional.of(user));
        when(encoder.matches("password", "hash")).thenReturn(true);
        assertSame(user, service.login("player1", "password"));
    }

    @Test void loginRejectsInvalidPassword() {
        when(users.findByUsername("player1")).thenReturn(Optional.of(player(1)));
        assertThrows(UserInvalidPasswordException.class, () -> service.login("player1", "wrong"));
    }

    @Test void missingUserIsRejectedByAllLookupMethods() {
        assertThrows(UserNotFoundException.class, () -> service.login("unknown", "password"));
        assertThrows(UsernameNotFoundException.class, () -> service.loadUserByUsername("unknown"));
        assertThrows(UserNotFoundException.class, () -> service.findById(1));
    }

    @Test void deletedUserCannotLoginOrBeLoadedForAuthentication() {
        UserEntity user = player(1);
        user.setDeleted(true);
        when(users.findByUsername("player1")).thenReturn(Optional.of(user));
        when(users.findWithRoleById(1)).thenReturn(Optional.of(user));
        assertThrows(UserNotFoundException.class, () -> service.login("player1", "password"));
        assertThrows(UsernameNotFoundException.class, () -> service.loadUserByUsername("player1"));
        assertThrows(UserNotFoundException.class, () -> service.findById(1));
        verifyNoInteractions(encoder);
    }

    @Test void activeUserCanBeLoadedByUsernameOrId() {
        UserEntity user = player(1);
        when(users.findByUsername("player1")).thenReturn(Optional.of(user));
        when(users.findWithRoleById(1)).thenReturn(Optional.of(user));
        assertSame(user, service.loadUserByUsername("player1"));
        assertSame(user, service.findById(1));
    }
}