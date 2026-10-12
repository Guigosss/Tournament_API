package com.technofuturtic.tournament_api.bll.services.impls;

import com.technofuturtic.tournament_api.api.models.tournament.responses.RegistrationCheckResponse;
import com.technofuturtic.tournament_api.api.models.tournament.responses.RegistrationResponse;
import com.technofuturtic.tournament_api.bll.exceptions.registration.RegistrationNotFoundException;
import com.technofuturtic.tournament_api.bll.exceptions.team.TeamConflictException;
import com.technofuturtic.tournament_api.bll.exceptions.team.TeamNotFoundException;
import com.technofuturtic.tournament_api.bll.exceptions.tournament.TournamentNotFoundException;
import com.technofuturtic.tournament_api.bll.exceptions.user.UserNotFoundException;
import com.technofuturtic.tournament_api.dal.repositories.ParticipantRepository;
import com.technofuturtic.tournament_api.dal.repositories.RegisterTeamRepository;
import com.technofuturtic.tournament_api.dal.repositories.RegisterUserRepository;
import com.technofuturtic.tournament_api.dal.repositories.TeamRepository;
import com.technofuturtic.tournament_api.dal.repositories.TournamentRepository;
import com.technofuturtic.tournament_api.dal.repositories.UserRepository;
import com.technofuturtic.tournament_api.dl.entities.ParticipantEntity;
import com.technofuturtic.tournament_api.dl.entities.RegisterTeamEntity;
import com.technofuturtic.tournament_api.dl.entities.RegisterUserEntity;
import com.technofuturtic.tournament_api.dl.entities.TeamEntity;
import com.technofuturtic.tournament_api.dl.entities.TournamentEntity;
import com.technofuturtic.tournament_api.dl.entities.UserEntity;
import com.technofuturtic.tournament_api.dl.enums.ParticipantType;
import com.technofuturtic.tournament_api.dl.enums.RegistrationStatus;
import com.technofuturtic.tournament_api.dl.enums.TournamentStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RegistrationServiceTest {

    @Mock private TournamentRepository tournamentRepository;
    @Mock private UserRepository userRepository;
    @Mock private TeamRepository teamRepository;
    @Mock private RegisterUserRepository registerUserRepository;
    @Mock private RegisterTeamRepository registerTeamRepository;
    @Mock private ParticipantRepository participantRepository;

    @InjectMocks
    private RegistrationServiceImpl service;

    private static final Integer TOURNAMENT_ID = 1;
    private static final Integer USER_ID = 2;
    private static final Integer TEAM_ID = 5;

    // ------------------------------------------------------------------
    // Méthodes utilitaires de construction des données de test.
    // Les entités n'ont pas de setter sur l'id : on le renseigne par réflexion.
    // Les dates d'inscription sont calculées par rapport à aujourd'hui, car le
    // service utilise LocalDate.now().
    // ------------------------------------------------------------------

    private static <T> T withId(T entity, Integer id) {
        ReflectionTestUtils.setField(entity, "id", id);
        return entity;
    }

    private TournamentEntity tournament(ParticipantType type, int max, TournamentStatus status,
                                        LocalDate registrationStart, LocalDate registrationEnd) {
        TournamentEntity tournament = new TournamentEntity();
        tournament.setParticipantType(type);
        tournament.setMaxParticipants(max);
        tournament.setStatus(status);
        tournament.setRegistrationStartDate(registrationStart);
        tournament.setRegistrationEndDate(registrationEnd);
        return withId(tournament, TOURNAMENT_ID);
    }

    //Tournoi dont la période d'inscription inclut aujourd'hui
    private TournamentEntity tournamentWithStatus(ParticipantType type, int max, TournamentStatus status) {
        LocalDate today = LocalDate.now();
        return tournament(type, max, status, today.minusDays(1), today.plusDays(5));
    }

    //Tournoi aux inscriptions ouvertes
    private TournamentEntity openTournament(ParticipantType type, int max) {
        return tournamentWithStatus(type, max, TournamentStatus.REGISTRATION_OPEN);
    }

    private UserEntity user(Integer id, String username) {
        UserEntity user = new UserEntity();
        user.setUsername(username);
        user.setEmail(username + "@test.com");
        return withId(user, id);
    }

    private TeamEntity team(Integer id, String name, int teamSize, int memberCount) {
        TeamEntity team = new TeamEntity();
        team.setName(name);
        team.setTeamSize(teamSize);
        for (int i = 0; i < memberCount; i++) {
            team.getMembers().add(user(100 + i, "member" + i));
        }
        return withId(team, id);
    }

    private RegisterUserEntity userRegistration(UserEntity user, TournamentEntity tournament, RegistrationStatus status) {
        RegisterUserEntity registration = new RegisterUserEntity();
        registration.setUser(user);
        registration.setTournament(tournament);
        registration.setRegisterDate(java.time.LocalDateTime.of(2026, 10, 1, 10, 30));
        registration.setStatus(status);
        return registration;
    }

    private RegisterTeamEntity teamRegistration(TeamEntity team, TournamentEntity tournament, RegistrationStatus status) {
        RegisterTeamEntity registration = new RegisterTeamEntity();
        registration.setTeam(team);
        registration.setTournament(tournament);
        registration.setRegisterDate(java.time.LocalDateTime.of(2026, 10, 1, 10, 30));
        registration.setStatus(status);
        return registration;
    }

    private ParticipantEntity participantOfUser(UserEntity user, TournamentEntity tournament) {
        ParticipantEntity participant = new ParticipantEntity();
        participant.setTournament(tournament);
        participant.setUser(user);
        return participant;
    }

    private ParticipantEntity participantOfTeam(TeamEntity team, TournamentEntity tournament) {
        ParticipantEntity participant = new ParticipantEntity();
        participant.setTournament(tournament);
        participant.setTeam(team);
        return participant;
    }

    //Une équipe archivée (on simule l'état « archivée » sans dépendre de la façon dont il est stocké)
    private TeamEntity archivedTeamMock() {
        TeamEntity team = mock(TeamEntity.class);
        when(team.isArchived()).thenReturn(true);
        return team;
    }

    //Le save du repository renvoie l'entité reçue, comme le ferait JPA
    private void echoUserSave() {
        when(registerUserRepository.save(any(RegisterUserEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }

    private void echoTeamSave() {
        when(registerTeamRepository.save(any(RegisterTeamEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }

    // ------------------------------------------------------------------
    // registrationPlayer
    // ------------------------------------------------------------------

    @Nested
    @DisplayName("registrationPlayer : inscrire un joueur")
    class RegistrationPlayer {

        @Test
        @DisplayName("tournoi inexistant : TournamentNotFoundException")
        void tournamentNotFound() {
            when(tournamentRepository.findById(99)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.registrationPlayer(99, USER_ID))
                    .isInstanceOf(TournamentNotFoundException.class);

            verifyNoInteractions(userRepository, registerUserRepository);
        }

        @Test
        @DisplayName("tournoi d'équipes : le joueur est refusé avant toute autre vérification")
        void wrongParticipantType() {
            when(tournamentRepository.findById(TOURNAMENT_ID))
                    .thenReturn(Optional.of(openTournament(ParticipantType.TEAM, 4)));

            assertThatThrownBy(() -> service.registrationPlayer(TOURNAMENT_ID, USER_ID))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("type de participant");

            verifyNoInteractions(userRepository, registerUserRepository);
        }

        @Test
        @DisplayName("joueur inexistant : UserNotFoundException")
        void userNotFound() {
            when(tournamentRepository.findById(TOURNAMENT_ID))
                    .thenReturn(Optional.of(openTournament(ParticipantType.PLAYER, 4)));
            when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.registrationPlayer(TOURNAMENT_ID, USER_ID))
                    .isInstanceOf(UserNotFoundException.class);

            verifyNoInteractions(registerUserRepository);
        }

        @ParameterizedTest
        @EnumSource(value = TournamentStatus.class, names = "REGISTRATION_OPEN", mode = EnumSource.Mode.EXCLUDE)
        @DisplayName("tournoi dont les inscriptions ne sont pas ouvertes (quel que soit le statut) : refusé")
        void registrationsNotOpen(TournamentStatus status) {
            when(tournamentRepository.findById(TOURNAMENT_ID))
                    .thenReturn(Optional.of(tournamentWithStatus(ParticipantType.PLAYER, 4, status)));
            when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user(USER_ID, "alice")));

            assertThatThrownBy(() -> service.registrationPlayer(TOURNAMENT_ID, USER_ID))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("pas ouvertes");

            verifyNoInteractions(registerUserRepository);
        }

        @Test
        @DisplayName("période d'inscription pas encore commencée : refusé")
        void registrationPeriodNotStarted() {
            LocalDate today = LocalDate.now();
            when(tournamentRepository.findById(TOURNAMENT_ID)).thenReturn(Optional.of(
                    tournament(ParticipantType.PLAYER, 4, TournamentStatus.REGISTRATION_OPEN,
                            today.plusDays(1), today.plusDays(5))));
            when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user(USER_ID, "alice")));

            assertThatThrownBy(() -> service.registrationPlayer(TOURNAMENT_ID, USER_ID))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("période");

            verifyNoInteractions(registerUserRepository);
        }

        @Test
        @DisplayName("période d'inscription terminée : refusé")
        void registrationPeriodOver() {
            LocalDate today = LocalDate.now();
            when(tournamentRepository.findById(TOURNAMENT_ID)).thenReturn(Optional.of(
                    tournament(ParticipantType.PLAYER, 4, TournamentStatus.REGISTRATION_OPEN,
                            today.minusDays(5), today.minusDays(1))));
            when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user(USER_ID, "alice")));

            assertThatThrownBy(() -> service.registrationPlayer(TOURNAMENT_ID, USER_ID))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("période");

            verifyNoInteractions(registerUserRepository);
        }

        @Test
        @DisplayName("dernier jour de la période d'inscription : accepté")
        void lastDayOfRegistrationPeriod() {
            LocalDate today = LocalDate.now();
            when(tournamentRepository.findById(TOURNAMENT_ID)).thenReturn(Optional.of(
                    tournament(ParticipantType.PLAYER, 4, TournamentStatus.REGISTRATION_OPEN,
                            today.minusDays(3), today)));
            when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user(USER_ID, "alice")));
            echoUserSave();

            RegistrationResponse response = service.registrationPlayer(TOURNAMENT_ID, USER_ID);

            assertThat(response.status()).isEqualTo(RegistrationStatus.PENDING);
        }

        @Test
        @DisplayName("joueur déjà inscrit : refusé, rien n'est enregistré")
        void alreadyRegistered() {
            when(tournamentRepository.findById(TOURNAMENT_ID))
                    .thenReturn(Optional.of(openTournament(ParticipantType.PLAYER, 4)));
            when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user(USER_ID, "alice")));
            when(registerUserRepository.existsByUserIdAndTournamentId(USER_ID, TOURNAMENT_ID)).thenReturn(true);

            assertThatThrownBy(() -> service.registrationPlayer(TOURNAMENT_ID, USER_ID))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("déjà inscrit");

            verify(registerUserRepository, never()).save(any(RegisterUserEntity.class));
        }

        @Test
        @DisplayName("tournoi complet (autant de VALIDATED que le maximum) : refusé")
        void tournamentFull() {
            when(tournamentRepository.findById(TOURNAMENT_ID))
                    .thenReturn(Optional.of(openTournament(ParticipantType.PLAYER, 4)));
            when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user(USER_ID, "alice")));
            when(registerUserRepository.countByTournamentIdAndStatus(TOURNAMENT_ID, RegistrationStatus.VALIDATED))
                    .thenReturn(4L);

            assertThatThrownBy(() -> service.registrationPlayer(TOURNAMENT_ID, USER_ID))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("complet");

            verify(registerUserRepository, never()).save(any(RegisterUserEntity.class));
        }

        @Test
        @DisplayName("il reste une place : accepté")
        void lastPlaceAvailable() {
            when(tournamentRepository.findById(TOURNAMENT_ID))
                    .thenReturn(Optional.of(openTournament(ParticipantType.PLAYER, 4)));
            when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user(USER_ID, "alice")));
            when(registerUserRepository.countByTournamentIdAndStatus(TOURNAMENT_ID, RegistrationStatus.VALIDATED))
                    .thenReturn(3L);
            echoUserSave();

            RegistrationResponse response = service.registrationPlayer(TOURNAMENT_ID, USER_ID);

            assertThat(response.status()).isEqualTo(RegistrationStatus.PENDING);
        }

        @Test
        @DisplayName("inscription valide : enregistrée en PENDING avec la date du jour")
        void validRegistration() {
            TournamentEntity tournament = openTournament(ParticipantType.PLAYER, 4);
            UserEntity alice = user(USER_ID, "alice");
            when(tournamentRepository.findById(TOURNAMENT_ID)).thenReturn(Optional.of(tournament));
            when(userRepository.findById(USER_ID)).thenReturn(Optional.of(alice));
            echoUserSave();

            RegistrationResponse response = service.registrationPlayer(TOURNAMENT_ID, USER_ID);

            assertThat(response.tournamentId()).isEqualTo(TOURNAMENT_ID);
            assertThat(response.userId()).isEqualTo(USER_ID);
            assertThat(response.username()).isEqualTo("alice");
            assertThat(response.teamId()).isNull();
            assertThat(response.status()).isEqualTo(RegistrationStatus.PENDING);
            assertThat(response.registerDate()).isNotNull();

            ArgumentCaptor<RegisterUserEntity> saved = ArgumentCaptor.forClass(RegisterUserEntity.class);
            verify(registerUserRepository).save(saved.capture());
            assertThat(saved.getValue().getUser()).isSameAs(alice);
            assertThat(saved.getValue().getTournament()).isSameAs(tournament);
            assertThat(saved.getValue().getStatus()).isEqualTo(RegistrationStatus.PENDING);
        }
    }

    // ------------------------------------------------------------------
    // registrationTeam
    // ------------------------------------------------------------------

    @Nested
    @DisplayName("registrationTeam : inscrire une équipe")
    class RegistrationTeam {

        @Test
        @DisplayName("tournoi inexistant : TournamentNotFoundException")
        void tournamentNotFound() {
            when(tournamentRepository.findById(99)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.registrationTeam(99, TEAM_ID))
                    .isInstanceOf(TournamentNotFoundException.class);

            verifyNoInteractions(teamRepository, registerTeamRepository);
        }

        @Test
        @DisplayName("tournoi de joueurs : l'équipe est refusée avant toute autre vérification")
        void wrongParticipantType() {
            when(tournamentRepository.findById(TOURNAMENT_ID))
                    .thenReturn(Optional.of(openTournament(ParticipantType.PLAYER, 4)));

            assertThatThrownBy(() -> service.registrationTeam(TOURNAMENT_ID, TEAM_ID))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("type de participant");

            verifyNoInteractions(teamRepository, registerTeamRepository);
        }

        @Test
        @DisplayName("équipe inexistante : TeamNotFoundException")
        void teamNotFound() {
            when(tournamentRepository.findById(TOURNAMENT_ID))
                    .thenReturn(Optional.of(openTournament(ParticipantType.TEAM, 4)));
            when(teamRepository.findByIdForUpdate(TEAM_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.registrationTeam(TOURNAMENT_ID, TEAM_ID))
                    .isInstanceOf(TeamNotFoundException.class);

            verifyNoInteractions(registerTeamRepository);
        }

        @Test
        @DisplayName("équipe archivée : TeamConflictException, rien n'est enregistré")
        void archivedTeam() {
            when(tournamentRepository.findById(TOURNAMENT_ID))
                    .thenReturn(Optional.of(openTournament(ParticipantType.TEAM, 4)));
            TeamEntity archived = archivedTeamMock();
            when(teamRepository.findByIdForUpdate(TEAM_ID)).thenReturn(Optional.of(archived));

            assertThatThrownBy(() -> service.registrationTeam(TOURNAMENT_ID, TEAM_ID))
                    .isInstanceOf(TeamConflictException.class);

            verifyNoInteractions(registerTeamRepository);
        }

        @ParameterizedTest
        @EnumSource(value = TournamentStatus.class, names = "REGISTRATION_OPEN", mode = EnumSource.Mode.EXCLUDE)
        @DisplayName("tournoi dont les inscriptions ne sont pas ouvertes (quel que soit le statut) : refusé")
        void registrationsNotOpen(TournamentStatus status) {
            when(tournamentRepository.findById(TOURNAMENT_ID))
                    .thenReturn(Optional.of(tournamentWithStatus(ParticipantType.TEAM, 4, status)));
            when(teamRepository.findByIdForUpdate(TEAM_ID))
                    .thenReturn(Optional.of(team(TEAM_ID, "Les Lions", 2, 2)));

            assertThatThrownBy(() -> service.registrationTeam(TOURNAMENT_ID, TEAM_ID))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("pas ouvertes");

            verifyNoInteractions(registerTeamRepository);
        }

        @Test
        @DisplayName("période d'inscription terminée : refusé")
        void registrationPeriodOver() {
            LocalDate today = LocalDate.now();
            when(tournamentRepository.findById(TOURNAMENT_ID)).thenReturn(Optional.of(
                    tournament(ParticipantType.TEAM, 4, TournamentStatus.REGISTRATION_OPEN,
                            today.minusDays(5), today.minusDays(1))));
            when(teamRepository.findByIdForUpdate(TEAM_ID))
                    .thenReturn(Optional.of(team(TEAM_ID, "Les Lions", 2, 2)));

            assertThatThrownBy(() -> service.registrationTeam(TOURNAMENT_ID, TEAM_ID))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("période");

            verifyNoInteractions(registerTeamRepository);
        }

        @Test
        @DisplayName("équipe déjà inscrite : refusé, rien n'est enregistré")
        void alreadyRegistered() {
            when(tournamentRepository.findById(TOURNAMENT_ID))
                    .thenReturn(Optional.of(openTournament(ParticipantType.TEAM, 4)));
            when(teamRepository.findByIdForUpdate(TEAM_ID))
                    .thenReturn(Optional.of(team(TEAM_ID, "Les Lions", 2, 2)));
            when(registerTeamRepository.existsByTeamIdAndTournamentId(TEAM_ID, TOURNAMENT_ID)).thenReturn(true);

            assertThatThrownBy(() -> service.registrationTeam(TOURNAMENT_ID, TEAM_ID))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("déjà inscrit");

            verify(registerTeamRepository, never()).save(any(RegisterTeamEntity.class));
        }

        @Test
        @DisplayName("équipe avec moins de membres que sa taille : refusé")
        void notEnoughMembers() {
            when(tournamentRepository.findById(TOURNAMENT_ID))
                    .thenReturn(Optional.of(openTournament(ParticipantType.TEAM, 4)));
            when(teamRepository.findByIdForUpdate(TEAM_ID))
                    .thenReturn(Optional.of(team(TEAM_ID, "Les Lions", 3, 2)));

            assertThatThrownBy(() -> service.registrationTeam(TOURNAMENT_ID, TEAM_ID))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("pas assez de membres");

            verify(registerTeamRepository, never()).save(any(RegisterTeamEntity.class));
        }

        @Test
        @DisplayName("tournoi complet (autant de VALIDATED que le maximum) : refusé")
        void tournamentFull() {
            when(tournamentRepository.findById(TOURNAMENT_ID))
                    .thenReturn(Optional.of(openTournament(ParticipantType.TEAM, 4)));
            when(teamRepository.findByIdForUpdate(TEAM_ID))
                    .thenReturn(Optional.of(team(TEAM_ID, "Les Lions", 2, 2)));
            when(registerTeamRepository.countByTournamentIdAndStatus(TOURNAMENT_ID, RegistrationStatus.VALIDATED))
                    .thenReturn(4L);

            assertThatThrownBy(() -> service.registrationTeam(TOURNAMENT_ID, TEAM_ID))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("complet");

            verify(registerTeamRepository, never()).save(any(RegisterTeamEntity.class));
        }

        @Test
        @DisplayName("équipe avec plus de membres que la taille requise : acceptée")
        void moreMembersThanRequired() {
            when(tournamentRepository.findById(TOURNAMENT_ID))
                    .thenReturn(Optional.of(openTournament(ParticipantType.TEAM, 4)));
            when(teamRepository.findByIdForUpdate(TEAM_ID))
                    .thenReturn(Optional.of(team(TEAM_ID, "Les Lions", 2, 3)));
            echoTeamSave();

            RegistrationResponse response = service.registrationTeam(TOURNAMENT_ID, TEAM_ID);

            assertThat(response.status()).isEqualTo(RegistrationStatus.PENDING);
        }

        @Test
        @DisplayName("inscription valide : enregistrée en PENDING avec la date du jour")
        void validRegistration() {
            TournamentEntity tournament = openTournament(ParticipantType.TEAM, 4);
            TeamEntity lions = team(TEAM_ID, "Les Lions", 2, 2);
            when(tournamentRepository.findById(TOURNAMENT_ID)).thenReturn(Optional.of(tournament));
            when(teamRepository.findByIdForUpdate(TEAM_ID)).thenReturn(Optional.of(lions));
            echoTeamSave();

            RegistrationResponse response = service.registrationTeam(TOURNAMENT_ID, TEAM_ID);

            assertThat(response.tournamentId()).isEqualTo(TOURNAMENT_ID);
            assertThat(response.teamId()).isEqualTo(TEAM_ID);
            assertThat(response.teamName()).isEqualTo("Les Lions");
            assertThat(response.userId()).isNull();
            assertThat(response.status()).isEqualTo(RegistrationStatus.PENDING);
            assertThat(response.registerDate()).isNotNull();

            ArgumentCaptor<RegisterTeamEntity> saved = ArgumentCaptor.forClass(RegisterTeamEntity.class);
            verify(registerTeamRepository).save(saved.capture());
            assertThat(saved.getValue().getTeam()).isSameAs(lions);
            assertThat(saved.getValue().getTournament()).isSameAs(tournament);
            assertThat(saved.getValue().getStatus()).isEqualTo(RegistrationStatus.PENDING);
        }
    }

    // ------------------------------------------------------------------
    // unregisterPlayer
    // ------------------------------------------------------------------

    @Nested
    @DisplayName("unregisterPlayer : désinscrire un joueur")
    class UnregisterPlayer {

        @Test
        @DisplayName("tournoi inexistant : TournamentNotFoundException")
        void tournamentNotFound() {
            when(tournamentRepository.findById(99)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.unregisterPlayer(99, USER_ID))
                    .isInstanceOf(TournamentNotFoundException.class);

            verifyNoInteractions(registerUserRepository, participantRepository);
        }

        @ParameterizedTest
        @EnumSource(value = TournamentStatus.class, names = "REGISTRATION_OPEN", mode = EnumSource.Mode.EXCLUDE)
        @DisplayName("inscriptions non ouvertes (quel que soit le statut) : refusé, rien n'est supprimé")
        void registrationsNotOpen(TournamentStatus status) {
            when(tournamentRepository.findById(TOURNAMENT_ID))
                    .thenReturn(Optional.of(tournamentWithStatus(ParticipantType.PLAYER, 4, status)));

            assertThatThrownBy(() -> service.unregisterPlayer(TOURNAMENT_ID, USER_ID))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("pas ouvertes");

            verifyNoInteractions(registerUserRepository, participantRepository);
        }

        @Test
        @DisplayName("aucune inscription : RegistrationNotFoundException")
        void registrationNotFound() {
            when(tournamentRepository.findById(TOURNAMENT_ID))
                    .thenReturn(Optional.of(openTournament(ParticipantType.PLAYER, 4)));
            when(registerUserRepository.findByUserIdAndTournamentId(USER_ID, TOURNAMENT_ID))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.unregisterPlayer(TOURNAMENT_ID, USER_ID))
                    .isInstanceOf(RegistrationNotFoundException.class);

            verifyNoInteractions(participantRepository);
        }

        @Test
        @DisplayName("joueur exclu : refusé, rien n'est supprimé")
        void excludedPlayerCannotUnregister() {
            TournamentEntity tournament = openTournament(ParticipantType.PLAYER, 4);
            when(tournamentRepository.findById(TOURNAMENT_ID)).thenReturn(Optional.of(tournament));
            when(registerUserRepository.findByUserIdAndTournamentId(USER_ID, TOURNAMENT_ID))
                    .thenReturn(Optional.of(userRegistration(user(USER_ID, "alice"), tournament, RegistrationStatus.EXCLUDED)));

            assertThatThrownBy(() -> service.unregisterPlayer(TOURNAMENT_ID, USER_ID))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("exclu");

            verify(registerUserRepository, never()).delete(any(RegisterUserEntity.class));
            verifyNoInteractions(participantRepository);
        }

        @Test
        @DisplayName("inscription PENDING (pas de participant) : seule l'inscription est supprimée")
        void pendingRegistration() {
            TournamentEntity tournament = openTournament(ParticipantType.PLAYER, 4);
            RegisterUserEntity registration =
                    userRegistration(user(USER_ID, "alice"), tournament, RegistrationStatus.PENDING);
            when(tournamentRepository.findById(TOURNAMENT_ID)).thenReturn(Optional.of(tournament));
            when(registerUserRepository.findByUserIdAndTournamentId(USER_ID, TOURNAMENT_ID))
                    .thenReturn(Optional.of(registration));
            when(participantRepository.findByUserIdAndTournamentId(USER_ID, TOURNAMENT_ID))
                    .thenReturn(Optional.empty());

            service.unregisterPlayer(TOURNAMENT_ID, USER_ID);

            verify(registerUserRepository).delete(registration);
            verify(participantRepository, never()).delete(any(ParticipantEntity.class));
        }

        @Test
        @DisplayName("inscription VALIDATED (avec participant) : le participant et l'inscription sont supprimés")
        void validatedRegistration() {
            TournamentEntity tournament = openTournament(ParticipantType.PLAYER, 4);
            UserEntity alice = user(USER_ID, "alice");
            RegisterUserEntity registration = userRegistration(alice, tournament, RegistrationStatus.VALIDATED);
            ParticipantEntity participant = participantOfUser(alice, tournament);
            when(tournamentRepository.findById(TOURNAMENT_ID)).thenReturn(Optional.of(tournament));
            when(registerUserRepository.findByUserIdAndTournamentId(USER_ID, TOURNAMENT_ID))
                    .thenReturn(Optional.of(registration));
            when(participantRepository.findByUserIdAndTournamentId(USER_ID, TOURNAMENT_ID))
                    .thenReturn(Optional.of(participant));

            service.unregisterPlayer(TOURNAMENT_ID, USER_ID);

            verify(participantRepository).delete(participant);
            verify(registerUserRepository).delete(registration);
        }
    }

    // ------------------------------------------------------------------
    // unregisterTeam
    // ------------------------------------------------------------------

    @Nested
    @DisplayName("unregisterTeam : désinscrire une équipe")
    class UnregisterTeam {

        @Test
        @DisplayName("tournoi inexistant : TournamentNotFoundException")
        void tournamentNotFound() {
            when(tournamentRepository.findById(99)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.unregisterTeam(99, TEAM_ID))
                    .isInstanceOf(TournamentNotFoundException.class);

            verifyNoInteractions(registerTeamRepository, participantRepository);
        }

        @ParameterizedTest
        @EnumSource(value = TournamentStatus.class, names = "REGISTRATION_OPEN", mode = EnumSource.Mode.EXCLUDE)
        @DisplayName("inscriptions non ouvertes (quel que soit le statut) : refusé, rien n'est supprimé")
        void registrationsNotOpen(TournamentStatus status) {
            when(tournamentRepository.findById(TOURNAMENT_ID))
                    .thenReturn(Optional.of(tournamentWithStatus(ParticipantType.TEAM, 4, status)));

            assertThatThrownBy(() -> service.unregisterTeam(TOURNAMENT_ID, TEAM_ID))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("pas ouvertes");

            verifyNoInteractions(registerTeamRepository, participantRepository);
        }

        @Test
        @DisplayName("aucune inscription : RegistrationNotFoundException")
        void registrationNotFound() {
            when(tournamentRepository.findById(TOURNAMENT_ID))
                    .thenReturn(Optional.of(openTournament(ParticipantType.TEAM, 4)));
            when(registerTeamRepository.findByTeamIdAndTournamentId(TEAM_ID, TOURNAMENT_ID))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.unregisterTeam(TOURNAMENT_ID, TEAM_ID))
                    .isInstanceOf(RegistrationNotFoundException.class);

            verifyNoInteractions(participantRepository);
        }

        @Test
        @DisplayName("équipe exclue : refusé, rien n'est supprimé")
        void excludedTeamCannotUnregister() {
            TournamentEntity tournament = openTournament(ParticipantType.TEAM, 4);
            when(tournamentRepository.findById(TOURNAMENT_ID)).thenReturn(Optional.of(tournament));
            when(registerTeamRepository.findByTeamIdAndTournamentId(TEAM_ID, TOURNAMENT_ID))
                    .thenReturn(Optional.of(teamRegistration(team(TEAM_ID, "Les Lions", 2, 2), tournament,
                            RegistrationStatus.EXCLUDED)));

            assertThatThrownBy(() -> service.unregisterTeam(TOURNAMENT_ID, TEAM_ID))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("exclue");

            verify(registerTeamRepository, never()).delete(any(RegisterTeamEntity.class));
            verifyNoInteractions(participantRepository);
        }

        @Test
        @DisplayName("inscription PENDING (pas de participant) : seule l'inscription est supprimée")
        void pendingRegistration() {
            TournamentEntity tournament = openTournament(ParticipantType.TEAM, 4);
            RegisterTeamEntity registration =
                    teamRegistration(team(TEAM_ID, "Les Lions", 2, 2), tournament, RegistrationStatus.PENDING);
            when(tournamentRepository.findById(TOURNAMENT_ID)).thenReturn(Optional.of(tournament));
            when(registerTeamRepository.findByTeamIdAndTournamentId(TEAM_ID, TOURNAMENT_ID))
                    .thenReturn(Optional.of(registration));
            when(participantRepository.findByTeamIdAndTournamentId(TEAM_ID, TOURNAMENT_ID))
                    .thenReturn(Optional.empty());

            service.unregisterTeam(TOURNAMENT_ID, TEAM_ID);

            verify(registerTeamRepository).delete(registration);
            verify(participantRepository, never()).delete(any(ParticipantEntity.class));
        }

        @Test
        @DisplayName("inscription VALIDATED (avec participant) : le participant et l'inscription sont supprimés")
        void validatedRegistration() {
            TournamentEntity tournament = openTournament(ParticipantType.TEAM, 4);
            TeamEntity lions = team(TEAM_ID, "Les Lions", 2, 2);
            RegisterTeamEntity registration = teamRegistration(lions, tournament, RegistrationStatus.VALIDATED);
            ParticipantEntity participant = participantOfTeam(lions, tournament);
            when(tournamentRepository.findById(TOURNAMENT_ID)).thenReturn(Optional.of(tournament));
            when(registerTeamRepository.findByTeamIdAndTournamentId(TEAM_ID, TOURNAMENT_ID))
                    .thenReturn(Optional.of(registration));
            when(participantRepository.findByTeamIdAndTournamentId(TEAM_ID, TOURNAMENT_ID))
                    .thenReturn(Optional.of(participant));

            service.unregisterTeam(TOURNAMENT_ID, TEAM_ID);

            verify(participantRepository).delete(participant);
            verify(registerTeamRepository).delete(registration);
        }
    }

    // ------------------------------------------------------------------
    // validatePlayer
    // ------------------------------------------------------------------

    @Nested
    @DisplayName("validatePlayer : valider l'inscription d'un joueur")
    class ValidatePlayer {

        @Test
        @DisplayName("tournoi inexistant : TournamentNotFoundException")
        void tournamentNotFound() {
            when(tournamentRepository.findById(99)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.validatePlayer(99, USER_ID))
                    .isInstanceOf(TournamentNotFoundException.class);

            verifyNoInteractions(registerUserRepository, participantRepository);
        }

        @ParameterizedTest
        @EnumSource(value = TournamentStatus.class,
                names = {"REGISTRATION_OPEN", "REGISTRATION_CLOSED"}, mode = EnumSource.Mode.EXCLUDE)
        @DisplayName("tournoi ni ouvert ni fermé aux inscriptions (déjà commencé, annulé...) : refusé")
        void registrationsNotManageable(TournamentStatus status) {
            when(tournamentRepository.findById(TOURNAMENT_ID))
                    .thenReturn(Optional.of(tournamentWithStatus(ParticipantType.PLAYER, 4, status)));

            assertThatThrownBy(() -> service.validatePlayer(TOURNAMENT_ID, USER_ID))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("ne peuvent plus être gérées");

            verifyNoInteractions(registerUserRepository, participantRepository);
        }

        @Test
        @DisplayName("inscription inexistante : RegistrationNotFoundException")
        void registrationNotFound() {
            when(tournamentRepository.findById(TOURNAMENT_ID))
                    .thenReturn(Optional.of(openTournament(ParticipantType.PLAYER, 4)));
            when(registerUserRepository.findByUserIdAndTournamentId(USER_ID, TOURNAMENT_ID))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.validatePlayer(TOURNAMENT_ID, USER_ID))
                    .isInstanceOf(RegistrationNotFoundException.class);

            verifyNoInteractions(participantRepository);
        }

        @ParameterizedTest
        @EnumSource(value = RegistrationStatus.class, names = "PENDING", mode = EnumSource.Mode.EXCLUDE)
        @DisplayName("inscription qui n'est pas en attente (déjà validée, exclue) : refusé, aucun participant créé")
        void registrationNotPending(RegistrationStatus status) {
            TournamentEntity tournament = openTournament(ParticipantType.PLAYER, 4);
            when(tournamentRepository.findById(TOURNAMENT_ID)).thenReturn(Optional.of(tournament));
            when(registerUserRepository.findByUserIdAndTournamentId(USER_ID, TOURNAMENT_ID))
                    .thenReturn(Optional.of(userRegistration(user(USER_ID, "alice"), tournament, status)));

            assertThatThrownBy(() -> service.validatePlayer(TOURNAMENT_ID, USER_ID))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("en attente");

            verify(participantRepository, never()).save(any(ParticipantEntity.class));
        }

        @Test
        @DisplayName("tournoi complet (autant de VALIDATED que le maximum) : refusé, aucun participant créé")
        void tournamentFull() {
            TournamentEntity tournament = openTournament(ParticipantType.PLAYER, 4);
            RegisterUserEntity registration =
                    userRegistration(user(USER_ID, "alice"), tournament, RegistrationStatus.PENDING);
            when(tournamentRepository.findById(TOURNAMENT_ID)).thenReturn(Optional.of(tournament));
            when(registerUserRepository.findByUserIdAndTournamentId(USER_ID, TOURNAMENT_ID))
                    .thenReturn(Optional.of(registration));
            when(registerUserRepository.countByTournamentIdAndStatus(TOURNAMENT_ID, RegistrationStatus.VALIDATED))
                    .thenReturn(4L);

            assertThatThrownBy(() -> service.validatePlayer(TOURNAMENT_ID, USER_ID))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("complet");

            assertThat(registration.getStatus()).isEqualTo(RegistrationStatus.PENDING);
            verify(participantRepository, never()).save(any(ParticipantEntity.class));
        }

        @ParameterizedTest
        @EnumSource(value = TournamentStatus.class, names = {"REGISTRATION_OPEN", "REGISTRATION_CLOSED"})
        @DisplayName("validation possible tant que le tournoi n'a pas commencé : VALIDATED et participant créé")
        void validRegistration(TournamentStatus status) {
            TournamentEntity tournament = tournamentWithStatus(ParticipantType.PLAYER, 4, status);
            UserEntity alice = user(USER_ID, "alice");
            RegisterUserEntity registration = userRegistration(alice, tournament, RegistrationStatus.PENDING);
            when(tournamentRepository.findById(TOURNAMENT_ID)).thenReturn(Optional.of(tournament));
            when(registerUserRepository.findByUserIdAndTournamentId(USER_ID, TOURNAMENT_ID))
                    .thenReturn(Optional.of(registration));
            when(registerUserRepository.countByTournamentIdAndStatus(TOURNAMENT_ID, RegistrationStatus.VALIDATED))
                    .thenReturn(3L);
            echoUserSave();

            RegistrationResponse response = service.validatePlayer(TOURNAMENT_ID, USER_ID);

            assertThat(response.status()).isEqualTo(RegistrationStatus.VALIDATED);
            assertThat(response.userId()).isEqualTo(USER_ID);
            assertThat(registration.getStatus()).isEqualTo(RegistrationStatus.VALIDATED);

            ArgumentCaptor<ParticipantEntity> participant = ArgumentCaptor.forClass(ParticipantEntity.class);
            verify(participantRepository).save(participant.capture());
            assertThat(participant.getValue().getTournament()).isSameAs(tournament);
            assertThat(participant.getValue().getUser()).isSameAs(alice);
            assertThat(participant.getValue().getTeam()).isNull();
        }
    }

    // ------------------------------------------------------------------
    // validateTeam
    // ------------------------------------------------------------------

    @Nested
    @DisplayName("validateTeam : valider l'inscription d'une équipe")
    class ValidateTeam {

        @Test
        @DisplayName("équipe inexistante : TeamNotFoundException, aucun participant créé")
        void teamNotFound() {
            when(teamRepository.findByIdForUpdate(TEAM_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.validateTeam(TOURNAMENT_ID, TEAM_ID))
                    .isInstanceOf(TeamNotFoundException.class);

            verifyNoInteractions(participantRepository);
        }

        @Test
        @DisplayName("équipe archivée : TeamConflictException, aucun participant créé")
        void archivedTeam() {
            TeamEntity archived = archivedTeamMock();
            when(teamRepository.findByIdForUpdate(TEAM_ID)).thenReturn(Optional.of(archived));

            assertThatThrownBy(() -> service.validateTeam(TOURNAMENT_ID, TEAM_ID))
                    .isInstanceOf(TeamConflictException.class);

            verifyNoInteractions(participantRepository);
        }

        @Test
        @DisplayName("tournoi inexistant : TournamentNotFoundException")
        void tournamentNotFound() {
            when(teamRepository.findByIdForUpdate(TEAM_ID))
                    .thenReturn(Optional.of(team(TEAM_ID, "Les Lions", 2, 2)));
            when(tournamentRepository.findById(99)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.validateTeam(99, TEAM_ID))
                    .isInstanceOf(TournamentNotFoundException.class);

            verifyNoInteractions(registerTeamRepository, participantRepository);
        }

        @ParameterizedTest
        @EnumSource(value = TournamentStatus.class,
                names = {"REGISTRATION_OPEN", "REGISTRATION_CLOSED"}, mode = EnumSource.Mode.EXCLUDE)
        @DisplayName("tournoi ni ouvert ni fermé aux inscriptions (déjà commencé, annulé...) : refusé")
        void registrationsNotManageable(TournamentStatus status) {
            when(teamRepository.findByIdForUpdate(TEAM_ID))
                    .thenReturn(Optional.of(team(TEAM_ID, "Les Lions", 2, 2)));
            when(tournamentRepository.findById(TOURNAMENT_ID))
                    .thenReturn(Optional.of(tournamentWithStatus(ParticipantType.TEAM, 4, status)));

            assertThatThrownBy(() -> service.validateTeam(TOURNAMENT_ID, TEAM_ID))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("ne peuvent plus être gérées");

            verifyNoInteractions(registerTeamRepository, participantRepository);
        }

        @Test
        @DisplayName("inscription inexistante : RegistrationNotFoundException")
        void registrationNotFound() {
            when(teamRepository.findByIdForUpdate(TEAM_ID))
                    .thenReturn(Optional.of(team(TEAM_ID, "Les Lions", 2, 2)));
            when(tournamentRepository.findById(TOURNAMENT_ID))
                    .thenReturn(Optional.of(openTournament(ParticipantType.TEAM, 4)));
            when(registerTeamRepository.findByTeamIdAndTournamentId(TEAM_ID, TOURNAMENT_ID))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.validateTeam(TOURNAMENT_ID, TEAM_ID))
                    .isInstanceOf(RegistrationNotFoundException.class);

            verifyNoInteractions(participantRepository);
        }

        @ParameterizedTest
        @EnumSource(value = RegistrationStatus.class, names = "PENDING", mode = EnumSource.Mode.EXCLUDE)
        @DisplayName("inscription qui n'est pas en attente (déjà validée, exclue) : refusé, aucun participant créé")
        void registrationNotPending(RegistrationStatus status) {
            TournamentEntity tournament = openTournament(ParticipantType.TEAM, 4);
            TeamEntity lions = team(TEAM_ID, "Les Lions", 2, 2);
            when(teamRepository.findByIdForUpdate(TEAM_ID)).thenReturn(Optional.of(lions));
            when(tournamentRepository.findById(TOURNAMENT_ID)).thenReturn(Optional.of(tournament));
            when(registerTeamRepository.findByTeamIdAndTournamentId(TEAM_ID, TOURNAMENT_ID))
                    .thenReturn(Optional.of(teamRegistration(lions, tournament, status)));

            assertThatThrownBy(() -> service.validateTeam(TOURNAMENT_ID, TEAM_ID))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("en attente");

            verify(participantRepository, never()).save(any(ParticipantEntity.class));
        }

        @Test
        @DisplayName("tournoi complet (autant de VALIDATED que le maximum) : refusé, aucun participant créé")
        void tournamentFull() {
            TournamentEntity tournament = openTournament(ParticipantType.TEAM, 4);
            TeamEntity lions = team(TEAM_ID, "Les Lions", 2, 2);
            RegisterTeamEntity registration = teamRegistration(lions, tournament, RegistrationStatus.PENDING);
            when(teamRepository.findByIdForUpdate(TEAM_ID)).thenReturn(Optional.of(lions));
            when(tournamentRepository.findById(TOURNAMENT_ID)).thenReturn(Optional.of(tournament));
            when(registerTeamRepository.findByTeamIdAndTournamentId(TEAM_ID, TOURNAMENT_ID))
                    .thenReturn(Optional.of(registration));
            when(registerTeamRepository.countByTournamentIdAndStatus(TOURNAMENT_ID, RegistrationStatus.VALIDATED))
                    .thenReturn(4L);

            assertThatThrownBy(() -> service.validateTeam(TOURNAMENT_ID, TEAM_ID))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("complet");

            assertThat(registration.getStatus()).isEqualTo(RegistrationStatus.PENDING);
            verify(participantRepository, never()).save(any(ParticipantEntity.class));
        }

        @ParameterizedTest
        @EnumSource(value = TournamentStatus.class, names = {"REGISTRATION_OPEN", "REGISTRATION_CLOSED"})
        @DisplayName("validation possible tant que le tournoi n'a pas commencé : VALIDATED et participant créé")
        void validRegistration(TournamentStatus status) {
            TournamentEntity tournament = tournamentWithStatus(ParticipantType.TEAM, 4, status);
            TeamEntity lions = team(TEAM_ID, "Les Lions", 2, 2);
            RegisterTeamEntity registration = teamRegistration(lions, tournament, RegistrationStatus.PENDING);
            when(teamRepository.findByIdForUpdate(TEAM_ID)).thenReturn(Optional.of(lions));
            when(tournamentRepository.findById(TOURNAMENT_ID)).thenReturn(Optional.of(tournament));
            when(registerTeamRepository.findByTeamIdAndTournamentId(TEAM_ID, TOURNAMENT_ID))
                    .thenReturn(Optional.of(registration));
            when(registerTeamRepository.countByTournamentIdAndStatus(TOURNAMENT_ID, RegistrationStatus.VALIDATED))
                    .thenReturn(3L);
            echoTeamSave();

            RegistrationResponse response = service.validateTeam(TOURNAMENT_ID, TEAM_ID);

            assertThat(response.status()).isEqualTo(RegistrationStatus.VALIDATED);
            assertThat(response.teamId()).isEqualTo(TEAM_ID);
            assertThat(registration.getStatus()).isEqualTo(RegistrationStatus.VALIDATED);

            ArgumentCaptor<ParticipantEntity> participant = ArgumentCaptor.forClass(ParticipantEntity.class);
            verify(participantRepository).save(participant.capture());
            assertThat(participant.getValue().getTournament()).isSameAs(tournament);
            assertThat(participant.getValue().getTeam()).isSameAs(lions);
            assertThat(participant.getValue().getUser()).isNull();
        }
    }

    // ------------------------------------------------------------------
    // excludePlayer
    // ------------------------------------------------------------------

    @Nested
    @DisplayName("excludePlayer : exclure un joueur")
    class ExcludePlayer {

        @Test
        @DisplayName("tournoi inexistant : TournamentNotFoundException")
        void tournamentNotFound() {
            when(tournamentRepository.findById(99)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.excludePlayer(99, USER_ID))
                    .isInstanceOf(TournamentNotFoundException.class);

            verifyNoInteractions(registerUserRepository, participantRepository);
        }

        @ParameterizedTest
        @EnumSource(value = TournamentStatus.class,
                names = {"REGISTRATION_OPEN", "REGISTRATION_CLOSED"}, mode = EnumSource.Mode.EXCLUDE)
        @DisplayName("tournoi ni ouvert ni fermé aux inscriptions (déjà commencé, annulé...) : refusé")
        void registrationsNotManageable(TournamentStatus status) {
            when(tournamentRepository.findById(TOURNAMENT_ID))
                    .thenReturn(Optional.of(tournamentWithStatus(ParticipantType.PLAYER, 4, status)));

            assertThatThrownBy(() -> service.excludePlayer(TOURNAMENT_ID, USER_ID))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("ne peuvent plus être gérées");

            verifyNoInteractions(registerUserRepository, participantRepository);
        }

        @Test
        @DisplayName("inscription inexistante : RegistrationNotFoundException")
        void registrationNotFound() {
            when(tournamentRepository.findById(TOURNAMENT_ID))
                    .thenReturn(Optional.of(openTournament(ParticipantType.PLAYER, 4)));
            when(registerUserRepository.findByUserIdAndTournamentId(USER_ID, TOURNAMENT_ID))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.excludePlayer(TOURNAMENT_ID, USER_ID))
                    .isInstanceOf(RegistrationNotFoundException.class);

            verifyNoInteractions(participantRepository);
        }

        @Test
        @DisplayName("joueur déjà exclu : refusé")
        void alreadyExcluded() {
            TournamentEntity tournament = openTournament(ParticipantType.PLAYER, 4);
            when(tournamentRepository.findById(TOURNAMENT_ID)).thenReturn(Optional.of(tournament));
            when(registerUserRepository.findByUserIdAndTournamentId(USER_ID, TOURNAMENT_ID))
                    .thenReturn(Optional.of(userRegistration(user(USER_ID, "alice"), tournament, RegistrationStatus.EXCLUDED)));

            assertThatThrownBy(() -> service.excludePlayer(TOURNAMENT_ID, USER_ID))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("déjà exclu");

            verify(registerUserRepository, never()).save(any(RegisterUserEntity.class));
            verifyNoInteractions(participantRepository);
        }

        @Test
        @DisplayName("inscription PENDING : passe en EXCLUDED, aucun participant à supprimer")
        void excludePendingRegistration() {
            TournamentEntity tournament = openTournament(ParticipantType.PLAYER, 4);
            RegisterUserEntity registration =
                    userRegistration(user(USER_ID, "alice"), tournament, RegistrationStatus.PENDING);
            when(tournamentRepository.findById(TOURNAMENT_ID)).thenReturn(Optional.of(tournament));
            when(registerUserRepository.findByUserIdAndTournamentId(USER_ID, TOURNAMENT_ID))
                    .thenReturn(Optional.of(registration));
            when(participantRepository.findByUserIdAndTournamentId(USER_ID, TOURNAMENT_ID))
                    .thenReturn(Optional.empty());
            echoUserSave();

            RegistrationResponse response = service.excludePlayer(TOURNAMENT_ID, USER_ID);

            assertThat(response.status()).isEqualTo(RegistrationStatus.EXCLUDED);
            assertThat(registration.getStatus()).isEqualTo(RegistrationStatus.EXCLUDED);
            verify(participantRepository, never()).delete(any(ParticipantEntity.class));
        }

        @ParameterizedTest
        @EnumSource(value = TournamentStatus.class, names = {"REGISTRATION_OPEN", "REGISTRATION_CLOSED"})
        @DisplayName("inscription VALIDATED : passe en EXCLUDED et le participant est supprimé")
        void excludeValidatedRegistration(TournamentStatus status) {
            TournamentEntity tournament = tournamentWithStatus(ParticipantType.PLAYER, 4, status);
            UserEntity alice = user(USER_ID, "alice");
            RegisterUserEntity registration = userRegistration(alice, tournament, RegistrationStatus.VALIDATED);
            ParticipantEntity participant = participantOfUser(alice, tournament);
            when(tournamentRepository.findById(TOURNAMENT_ID)).thenReturn(Optional.of(tournament));
            when(registerUserRepository.findByUserIdAndTournamentId(USER_ID, TOURNAMENT_ID))
                    .thenReturn(Optional.of(registration));
            when(participantRepository.findByUserIdAndTournamentId(USER_ID, TOURNAMENT_ID))
                    .thenReturn(Optional.of(participant));
            echoUserSave();

            RegistrationResponse response = service.excludePlayer(TOURNAMENT_ID, USER_ID);

            assertThat(response.status()).isEqualTo(RegistrationStatus.EXCLUDED);
            assertThat(registration.getStatus()).isEqualTo(RegistrationStatus.EXCLUDED);
            verify(participantRepository).delete(participant);
        }
    }

    // ------------------------------------------------------------------
    // excludeTeam
    // ------------------------------------------------------------------

    @Nested
    @DisplayName("excludeTeam : exclure une équipe")
    class ExcludeTeam {

        @Test
        @DisplayName("tournoi inexistant : TournamentNotFoundException")
        void tournamentNotFound() {
            when(tournamentRepository.findById(99)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.excludeTeam(99, TEAM_ID))
                    .isInstanceOf(TournamentNotFoundException.class);

            verifyNoInteractions(registerTeamRepository, participantRepository);
        }

        @ParameterizedTest
        @EnumSource(value = TournamentStatus.class,
                names = {"REGISTRATION_OPEN", "REGISTRATION_CLOSED"}, mode = EnumSource.Mode.EXCLUDE)
        @DisplayName("tournoi ni ouvert ni fermé aux inscriptions (déjà commencé, annulé...) : refusé")
        void registrationsNotManageable(TournamentStatus status) {
            when(tournamentRepository.findById(TOURNAMENT_ID))
                    .thenReturn(Optional.of(tournamentWithStatus(ParticipantType.TEAM, 4, status)));

            assertThatThrownBy(() -> service.excludeTeam(TOURNAMENT_ID, TEAM_ID))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("ne peuvent plus être gérées");

            verifyNoInteractions(registerTeamRepository, participantRepository);
        }

        @Test
        @DisplayName("inscription inexistante : RegistrationNotFoundException")
        void registrationNotFound() {
            when(tournamentRepository.findById(TOURNAMENT_ID))
                    .thenReturn(Optional.of(openTournament(ParticipantType.TEAM, 4)));
            when(registerTeamRepository.findByTeamIdAndTournamentId(TEAM_ID, TOURNAMENT_ID))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.excludeTeam(TOURNAMENT_ID, TEAM_ID))
                    .isInstanceOf(RegistrationNotFoundException.class);

            verifyNoInteractions(participantRepository);
        }

        @Test
        @DisplayName("équipe déjà exclue : refusé")
        void alreadyExcluded() {
            TournamentEntity tournament = openTournament(ParticipantType.TEAM, 4);
            when(tournamentRepository.findById(TOURNAMENT_ID)).thenReturn(Optional.of(tournament));
            when(registerTeamRepository.findByTeamIdAndTournamentId(TEAM_ID, TOURNAMENT_ID))
                    .thenReturn(Optional.of(teamRegistration(team(TEAM_ID, "Les Lions", 2, 2), tournament,
                            RegistrationStatus.EXCLUDED)));

            assertThatThrownBy(() -> service.excludeTeam(TOURNAMENT_ID, TEAM_ID))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("déjà exclu");

            verify(registerTeamRepository, never()).save(any(RegisterTeamEntity.class));
            verifyNoInteractions(participantRepository);
        }

        @Test
        @DisplayName("inscription PENDING : passe en EXCLUDED, aucun participant à supprimer")
        void excludePendingRegistration() {
            TournamentEntity tournament = openTournament(ParticipantType.TEAM, 4);
            RegisterTeamEntity registration =
                    teamRegistration(team(TEAM_ID, "Les Lions", 2, 2), tournament, RegistrationStatus.PENDING);
            when(tournamentRepository.findById(TOURNAMENT_ID)).thenReturn(Optional.of(tournament));
            when(registerTeamRepository.findByTeamIdAndTournamentId(TEAM_ID, TOURNAMENT_ID))
                    .thenReturn(Optional.of(registration));
            when(participantRepository.findByTeamIdAndTournamentId(TEAM_ID, TOURNAMENT_ID))
                    .thenReturn(Optional.empty());
            echoTeamSave();

            RegistrationResponse response = service.excludeTeam(TOURNAMENT_ID, TEAM_ID);

            assertThat(response.status()).isEqualTo(RegistrationStatus.EXCLUDED);
            assertThat(registration.getStatus()).isEqualTo(RegistrationStatus.EXCLUDED);
            verify(participantRepository, never()).delete(any(ParticipantEntity.class));
        }

        @ParameterizedTest
        @EnumSource(value = TournamentStatus.class, names = {"REGISTRATION_OPEN", "REGISTRATION_CLOSED"})
        @DisplayName("inscription VALIDATED : passe en EXCLUDED et le participant est supprimé")
        void excludeValidatedRegistration(TournamentStatus status) {
            TournamentEntity tournament = tournamentWithStatus(ParticipantType.TEAM, 4, status);
            TeamEntity lions = team(TEAM_ID, "Les Lions", 2, 2);
            RegisterTeamEntity registration = teamRegistration(lions, tournament, RegistrationStatus.VALIDATED);
            ParticipantEntity participant = participantOfTeam(lions, tournament);
            when(tournamentRepository.findById(TOURNAMENT_ID)).thenReturn(Optional.of(tournament));
            when(registerTeamRepository.findByTeamIdAndTournamentId(TEAM_ID, TOURNAMENT_ID))
                    .thenReturn(Optional.of(registration));
            when(participantRepository.findByTeamIdAndTournamentId(TEAM_ID, TOURNAMENT_ID))
                    .thenReturn(Optional.of(participant));
            echoTeamSave();

            RegistrationResponse response = service.excludeTeam(TOURNAMENT_ID, TEAM_ID);

            assertThat(response.status()).isEqualTo(RegistrationStatus.EXCLUDED);
            assertThat(registration.getStatus()).isEqualTo(RegistrationStatus.EXCLUDED);
            verify(participantRepository).delete(participant);
        }
    }

    // ------------------------------------------------------------------
    // getRegistrations
    // ------------------------------------------------------------------

    @Nested
    @DisplayName("getRegistrations : liste des inscriptions, filtrable par statut")
    class GetRegistrations {

        @Test
        @DisplayName("tournoi inexistant : TournamentNotFoundException")
        void tournamentNotFound() {
            when(tournamentRepository.findById(99)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.getRegistrations(99, null))
                    .isInstanceOf(TournamentNotFoundException.class);

            verifyNoInteractions(registerUserRepository, registerTeamRepository);
        }

        @Test
        @DisplayName("tournoi de joueurs, sans filtre : toutes les inscriptions des joueurs")
        void playersWithoutFilter() {
            TournamentEntity tournament = openTournament(ParticipantType.PLAYER, 4);
            when(tournamentRepository.findById(TOURNAMENT_ID)).thenReturn(Optional.of(tournament));
            when(registerUserRepository.findByTournamentId(TOURNAMENT_ID)).thenReturn(List.of(
                    userRegistration(user(2, "alice"), tournament, RegistrationStatus.PENDING),
                    userRegistration(user(3, "bob"), tournament, RegistrationStatus.VALIDATED)));

            List<RegistrationResponse> result = service.getRegistrations(TOURNAMENT_ID, null);

            assertThat(result).extracting(RegistrationResponse::username).containsExactly("alice", "bob");
            assertThat(result).extracting(RegistrationResponse::status)
                    .containsExactly(RegistrationStatus.PENDING, RegistrationStatus.VALIDATED);
            verifyNoInteractions(registerTeamRepository);
        }

        @Test
        @DisplayName("tournoi de joueurs, filtre PENDING : seules les inscriptions de ce statut sont demandées")
        void playersWithFilter() {
            TournamentEntity tournament = openTournament(ParticipantType.PLAYER, 4);
            when(tournamentRepository.findById(TOURNAMENT_ID)).thenReturn(Optional.of(tournament));
            when(registerUserRepository.findByTournamentIdAndStatus(TOURNAMENT_ID, RegistrationStatus.PENDING))
                    .thenReturn(List.of(userRegistration(user(2, "alice"), tournament, RegistrationStatus.PENDING)));

            List<RegistrationResponse> result = service.getRegistrations(TOURNAMENT_ID, RegistrationStatus.PENDING);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).username()).isEqualTo("alice");
            verify(registerUserRepository, never()).findByTournamentId(TOURNAMENT_ID);
            verifyNoInteractions(registerTeamRepository);
        }

        @Test
        @DisplayName("tournoi d'équipes, sans filtre : toutes les inscriptions des équipes")
        void teamsWithoutFilter() {
            TournamentEntity tournament = openTournament(ParticipantType.TEAM, 4);
            when(tournamentRepository.findById(TOURNAMENT_ID)).thenReturn(Optional.of(tournament));
            when(registerTeamRepository.findByTournamentId(TOURNAMENT_ID)).thenReturn(List.of(
                    teamRegistration(team(TEAM_ID, "Les Lions", 2, 2), tournament, RegistrationStatus.PENDING),
                    teamRegistration(team(6, "Les Aigles", 2, 2), tournament, RegistrationStatus.EXCLUDED)));

            List<RegistrationResponse> result = service.getRegistrations(TOURNAMENT_ID, null);

            assertThat(result).extracting(RegistrationResponse::teamName).containsExactly("Les Lions", "Les Aigles");
            verifyNoInteractions(registerUserRepository);
        }

        @Test
        @DisplayName("tournoi d'équipes, filtre EXCLUDED : seules les inscriptions de ce statut sont demandées")
        void teamsWithFilter() {
            TournamentEntity tournament = openTournament(ParticipantType.TEAM, 4);
            when(tournamentRepository.findById(TOURNAMENT_ID)).thenReturn(Optional.of(tournament));
            when(registerTeamRepository.findByTournamentIdAndStatus(TOURNAMENT_ID, RegistrationStatus.EXCLUDED))
                    .thenReturn(List.of(
                            teamRegistration(team(6, "Les Aigles", 2, 2), tournament, RegistrationStatus.EXCLUDED)));

            List<RegistrationResponse> result = service.getRegistrations(TOURNAMENT_ID, RegistrationStatus.EXCLUDED);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).teamName()).isEqualTo("Les Aigles");
            verify(registerTeamRepository, never()).findByTournamentId(TOURNAMENT_ID);
            verifyNoInteractions(registerUserRepository);
        }

        @Test
        @DisplayName("aucune inscription : liste vide")
        void noRegistration() {
            when(tournamentRepository.findById(TOURNAMENT_ID))
                    .thenReturn(Optional.of(openTournament(ParticipantType.PLAYER, 4)));
            when(registerUserRepository.findByTournamentId(TOURNAMENT_ID)).thenReturn(List.of());

            assertThat(service.getRegistrations(TOURNAMENT_ID, null)).isEmpty();
        }
    }

    // ------------------------------------------------------------------
    // checkPlayerRegistration / checkTeamRegistration
    // ------------------------------------------------------------------

    @Nested
    @DisplayName("checkPlayerRegistration : un joueur est-il inscrit ?")
    class CheckPlayerRegistration {

        @Test
        @DisplayName("tournoi inexistant : TournamentNotFoundException")
        void tournamentNotFound() {
            when(tournamentRepository.existsById(99)).thenReturn(false);

            assertThatThrownBy(() -> service.checkPlayerRegistration(99, USER_ID))
                    .isInstanceOf(TournamentNotFoundException.class);

            verifyNoInteractions(registerUserRepository);
        }

        @ParameterizedTest
        @EnumSource(RegistrationStatus.class)
        @DisplayName("joueur inscrit, quel que soit le statut : registered = true avec ce statut")
        void registered(RegistrationStatus status) {
            TournamentEntity tournament = openTournament(ParticipantType.PLAYER, 4);
            when(tournamentRepository.existsById(TOURNAMENT_ID)).thenReturn(true);
            when(registerUserRepository.findByUserIdAndTournamentId(USER_ID, TOURNAMENT_ID))
                    .thenReturn(Optional.of(userRegistration(user(USER_ID, "alice"), tournament, status)));

            RegistrationCheckResponse response = service.checkPlayerRegistration(TOURNAMENT_ID, USER_ID);

            assertThat(response.registered()).isTrue();
            assertThat(response.status()).isEqualTo(status);
        }

        @Test
        @DisplayName("joueur non inscrit : registered = false, statut null (pas d'exception)")
        void notRegistered() {
            when(tournamentRepository.existsById(TOURNAMENT_ID)).thenReturn(true);
            when(registerUserRepository.findByUserIdAndTournamentId(USER_ID, TOURNAMENT_ID))
                    .thenReturn(Optional.empty());

            RegistrationCheckResponse response = service.checkPlayerRegistration(TOURNAMENT_ID, USER_ID);

            assertThat(response.registered()).isFalse();
            assertThat(response.status()).isNull();
        }
    }

    @Nested
    @DisplayName("checkTeamRegistration : une équipe est-elle inscrite ?")
    class CheckTeamRegistration {

        @Test
        @DisplayName("tournoi inexistant : TournamentNotFoundException")
        void tournamentNotFound() {
            when(tournamentRepository.existsById(99)).thenReturn(false);

            assertThatThrownBy(() -> service.checkTeamRegistration(99, TEAM_ID))
                    .isInstanceOf(TournamentNotFoundException.class);

            verifyNoInteractions(registerTeamRepository);
        }

        @ParameterizedTest
        @EnumSource(RegistrationStatus.class)
        @DisplayName("équipe inscrite, quel que soit le statut : registered = true avec ce statut")
        void registered(RegistrationStatus status) {
            TournamentEntity tournament = openTournament(ParticipantType.TEAM, 4);
            when(tournamentRepository.existsById(TOURNAMENT_ID)).thenReturn(true);
            when(registerTeamRepository.findByTeamIdAndTournamentId(TEAM_ID, TOURNAMENT_ID))
                    .thenReturn(Optional.of(teamRegistration(team(TEAM_ID, "Les Lions", 2, 2), tournament, status)));

            RegistrationCheckResponse response = service.checkTeamRegistration(TOURNAMENT_ID, TEAM_ID);

            assertThat(response.registered()).isTrue();
            assertThat(response.status()).isEqualTo(status);
        }

        @Test
        @DisplayName("équipe non inscrite : registered = false, statut null (pas d'exception)")
        void notRegistered() {
            when(tournamentRepository.existsById(TOURNAMENT_ID)).thenReturn(true);
            when(registerTeamRepository.findByTeamIdAndTournamentId(TEAM_ID, TOURNAMENT_ID))
                    .thenReturn(Optional.empty());

            RegistrationCheckResponse response = service.checkTeamRegistration(TOURNAMENT_ID, TEAM_ID);

            assertThat(response.registered()).isFalse();
            assertThat(response.status()).isNull();
        }
    }
}