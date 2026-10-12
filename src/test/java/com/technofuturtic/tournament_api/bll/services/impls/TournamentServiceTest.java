package com.technofuturtic.tournament_api.bll.services.impls;

import com.technofuturtic.tournament_api.api.models.UserContext;
import com.technofuturtic.tournament_api.api.models.tournament.requests.TournamentRequest;
import com.technofuturtic.tournament_api.api.models.tournament.requests.TournamentUpdateRequest;
import com.technofuturtic.tournament_api.api.models.tournament.responses.ParticipantCountResponse;
import com.technofuturtic.tournament_api.api.models.tournament.responses.ParticipantResponse;
import com.technofuturtic.tournament_api.api.models.tournament.responses.TournamentDetailResponse;
import com.technofuturtic.tournament_api.api.models.tournament.responses.TournamentResponse;
import com.technofuturtic.tournament_api.bll.exceptions.tournament.TournamentNotFoundException;
import com.technofuturtic.tournament_api.bll.exceptions.user.UserNotFoundException;
import com.technofuturtic.tournament_api.bll.services.ParticipantService;
import com.technofuturtic.tournament_api.dal.repositories.RegisterTeamRepository;
import com.technofuturtic.tournament_api.dal.repositories.RegisterUserRepository;
import com.technofuturtic.tournament_api.dal.repositories.TournamentRepository;
import com.technofuturtic.tournament_api.dal.repositories.UserRepository;
import com.technofuturtic.tournament_api.dl.entities.TournamentEntity;
import com.technofuturtic.tournament_api.dl.entities.UserEntity;
import com.technofuturtic.tournament_api.dl.enums.ParticipantType;
import com.technofuturtic.tournament_api.dl.enums.RegistrationStatus;
import com.technofuturtic.tournament_api.dl.enums.TournamentFormat;
import com.technofuturtic.tournament_api.dl.enums.TournamentStatus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
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
class TournamentServiceTest {

    @Mock private TournamentRepository tournamentRepository;
    @Mock private UserRepository userRepository;
    @Mock private RegisterUserRepository registerUserRepository;
    @Mock private RegisterTeamRepository registerTeamRepository;
    @Mock private ParticipantService participantService;

    @InjectMocks
    private TournamentServiceImpl service;

    private static final Integer TOURNAMENT_ID = 1;

    //Le contexte de sécurité est statique : on le vide après chaque test pour ne pas polluer les autres
    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    // ------------------------------------------------------------------
    // Méthodes utilitaires de construction des données de test.
    // Les entités n'ont pas de setter sur l'id : on le renseigne par réflexion.
    // ------------------------------------------------------------------

    private static <T> T withId(T entity, Integer id) {
        ReflectionTestUtils.setField(entity, "id", id);
        return entity;
    }

    private UserEntity user(Integer id, String username) {
        UserEntity user = new UserEntity();
        user.setUsername(username);
        user.setEmail(username + "@test.com");
        return withId(user, id);
    }

    private TournamentEntity tournament(TournamentStatus status, UserEntity organizer) {
        TournamentEntity tournament = new TournamentEntity();
        tournament.setName("Tournoi de printemps");
        tournament.setDescription("Premier tournoi de la saison");
        tournament.setMaxParticipants(8);
        tournament.setFormat(TournamentFormat.SINGLE_ELIMINATION);
        tournament.setParticipantType(ParticipantType.PLAYER);
        tournament.setStatus(status);
        tournament.setStartDate(LocalDate.of(2026, 11, 15));
        tournament.setEndDate(LocalDate.of(2026, 11, 16));
        tournament.setRegistrationStartDate(LocalDate.of(2026, 10, 1));
        tournament.setRegistrationEndDate(LocalDate.of(2026, 11, 10));
        tournament.setOrganizer(organizer);
        return withId(tournament, TOURNAMENT_ID);
    }

    private TournamentEntity tournament(TournamentStatus status) {
        return tournament(status, user(1, "organizer"));
    }

    //Requête de création avec des dates valides
    private TournamentRequest createRequest() {
        return createRequest(LocalDate.of(2026, 11, 15), LocalDate.of(2026, 11, 16),
                LocalDate.of(2026, 10, 1), LocalDate.of(2026, 11, 10));
    }

    //Le dernier argument est l'organizerId de la requête : si ton TournamentRequest n'a plus ce champ,
    //retire-le ici (c'est le seul endroit qui construit une TournamentRequest).
    private TournamentRequest createRequest(LocalDate start, LocalDate end,
                                            LocalDate registrationStart, LocalDate registrationEnd) {
        return new TournamentRequest("Tournoi de printemps", "Premier tournoi de la saison", 8,
                TournamentFormat.SINGLE_ELIMINATION, ParticipantType.PLAYER,
                start, end, registrationStart, registrationEnd);
    }

    private TournamentUpdateRequest updateRequest(int max, ParticipantType type) {
        return updateRequest(max, type, LocalDate.of(2026, 12, 1), LocalDate.of(2026, 12, 2),
                LocalDate.of(2026, 11, 1), LocalDate.of(2026, 11, 25));
    }

    private TournamentUpdateRequest updateRequest(int max, ParticipantType type,
                                                  LocalDate start, LocalDate end,
                                                  LocalDate registrationStart, LocalDate registrationEnd) {
        return new TournamentUpdateRequest("Nouveau nom", "Nouvelle description", max,
                TournamentFormat.GROUPS_THEN_PLAYOFF, type, start, end, registrationStart, registrationEnd);
    }

    //Simule l'utilisateur connecté, tel que le JwtFilter le placerait dans le contexte de sécurité
    private void authenticateAs(Integer userId) {
        UserContext userContext = mock(UserContext.class);
        when(userContext.id()).thenReturn(userId);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(userContext, null, List.of()));
    }

    //Le save du repository renvoie l'entité reçue, comme le ferait JPA
    private void echoSave() {
        when(tournamentRepository.save(any(TournamentEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }

    // ------------------------------------------------------------------
    // create
    // ------------------------------------------------------------------

    @Nested
    @DisplayName("create : créer un tournoi")
    class Create {

        @Test
        @DisplayName("fin du tournoi avant son début : refusé, rien n'est lu ni enregistré")
        void endBeforeStart() {
            TournamentRequest request = createRequest(LocalDate.of(2026, 11, 16), LocalDate.of(2026, 11, 15),
                    LocalDate.of(2026, 10, 1), LocalDate.of(2026, 11, 10));

            assertThatThrownBy(() -> service.create(request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("date de fin");

            verifyNoInteractions(userRepository, tournamentRepository);
        }

        @Test
        @DisplayName("fin des inscriptions avant leur début : refusé")
        void registrationEndBeforeRegistrationStart() {
            TournamentRequest request = createRequest(LocalDate.of(2026, 11, 15), LocalDate.of(2026, 11, 16),
                    LocalDate.of(2026, 11, 10), LocalDate.of(2026, 10, 1));

            assertThatThrownBy(() -> service.create(request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("fin des inscriptions");

            verifyNoInteractions(userRepository, tournamentRepository);
        }

        @Test
        @DisplayName("inscriptions qui se terminent après le début du tournoi : refusé")
        void registrationEndsAfterTournamentStart() {
            TournamentRequest request = createRequest(LocalDate.of(2026, 11, 15), LocalDate.of(2026, 11, 16),
                    LocalDate.of(2026, 10, 1), LocalDate.of(2026, 11, 16));

            assertThatThrownBy(() -> service.create(request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("avant le début");

            verifyNoInteractions(userRepository, tournamentRepository);
        }

        @Test
        @DisplayName("inscriptions qui se terminent le jour du début du tournoi : accepté")
        void registrationEndsOnTournamentStart() {
            authenticateAs(1);
            when(userRepository.findById(1)).thenReturn(Optional.of(user(1, "organizer")));
            echoSave();

            TournamentResponse response = service.create(createRequest(
                    LocalDate.of(2026, 11, 15), LocalDate.of(2026, 11, 16),
                    LocalDate.of(2026, 10, 1), LocalDate.of(2026, 11, 15)));

            assertThat(response.status()).isEqualTo(TournamentStatus.UPCOMING);
        }

        @Test
        @DisplayName("utilisateur connecté introuvable en base : UserNotFoundException, rien n'est enregistré")
        void connectedUserNotFound() {
            authenticateAs(1);
            when(userRepository.findById(1)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.create(createRequest()))
                    .isInstanceOf(UserNotFoundException.class);

            verifyNoInteractions(tournamentRepository);
        }

        @Test
        @DisplayName("l'organisateur est l'utilisateur connecté, pas celui de la requête")
        void organizerComesFromSecurityContext() {
            UserEntity connected = user(1, "organizer");
            authenticateAs(1);
            when(userRepository.findById(1)).thenReturn(Optional.of(connected));
            echoSave();

            service.create(createRequest());

            ArgumentCaptor<TournamentEntity> saved = ArgumentCaptor.forClass(TournamentEntity.class);
            verify(tournamentRepository).save(saved.capture());
            assertThat(saved.getValue().getOrganizer()).isSameAs(connected);
            verify(userRepository, never()).findById(99);
        }

        @Test
        @DisplayName("création valide : toutes les informations de la requête, statut UPCOMING")
        void validCreation() {
            UserEntity organizer = user(1, "organizer");
            authenticateAs(1);
            when(userRepository.findById(1)).thenReturn(Optional.of(organizer));
            echoSave();

            TournamentResponse response = service.create(createRequest());

            ArgumentCaptor<TournamentEntity> saved = ArgumentCaptor.forClass(TournamentEntity.class);
            verify(tournamentRepository).save(saved.capture());
            TournamentEntity tournament = saved.getValue();
            assertThat(tournament.getName()).isEqualTo("Tournoi de printemps");
            assertThat(tournament.getDescription()).isEqualTo("Premier tournoi de la saison");
            assertThat(tournament.getMaxParticipants()).isEqualTo(8);
            assertThat(tournament.getFormat()).isEqualTo(TournamentFormat.SINGLE_ELIMINATION);
            assertThat(tournament.getParticipantType()).isEqualTo(ParticipantType.PLAYER);
            assertThat(tournament.getStartDate()).isEqualTo(LocalDate.of(2026, 11, 15));
            assertThat(tournament.getEndDate()).isEqualTo(LocalDate.of(2026, 11, 16));
            assertThat(tournament.getRegistrationStartDate()).isEqualTo(LocalDate.of(2026, 10, 1));
            assertThat(tournament.getRegistrationEndDate()).isEqualTo(LocalDate.of(2026, 11, 10));
            assertThat(tournament.getStatus()).isEqualTo(TournamentStatus.UPCOMING);

            assertThat(response.name()).isEqualTo("Tournoi de printemps");
            assertThat(response.status()).isEqualTo(TournamentStatus.UPCOMING);
            assertThat(response.organizerId()).isEqualTo(1);
            assertThat(response.organizerUsername()).isEqualTo("organizer");
        }
    }

    // ------------------------------------------------------------------
    // getById
    // ------------------------------------------------------------------

    @Nested
    @DisplayName("getById : consulter un tournoi")
    class GetById {

        @Test
        @DisplayName("tournoi inexistant : TournamentNotFoundException")
        void notFound() {
            when(tournamentRepository.findById(99)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.getById(99))
                    .isInstanceOf(TournamentNotFoundException.class);
        }

        @Test
        @DisplayName("tournoi existant : informations générales et organisateur")
        void found() {
            when(tournamentRepository.findById(TOURNAMENT_ID))
                    .thenReturn(Optional.of(tournament(TournamentStatus.REGISTRATION_OPEN)));

            TournamentResponse response = service.getById(TOURNAMENT_ID);

            assertThat(response.id()).isEqualTo(TOURNAMENT_ID);
            assertThat(response.name()).isEqualTo("Tournoi de printemps");
            assertThat(response.description()).isEqualTo("Premier tournoi de la saison");
            assertThat(response.maxParticipants()).isEqualTo(8);
            assertThat(response.format()).isEqualTo(TournamentFormat.SINGLE_ELIMINATION);
            assertThat(response.status()).isEqualTo(TournamentStatus.REGISTRATION_OPEN);
            assertThat(response.participantType()).isEqualTo(ParticipantType.PLAYER);
            assertThat(response.startDate()).isEqualTo(LocalDate.of(2026, 11, 15));
            assertThat(response.endDate()).isEqualTo(LocalDate.of(2026, 11, 16));
            assertThat(response.registrationStartDate()).isEqualTo(LocalDate.of(2026, 10, 1));
            assertThat(response.registrationEndDate()).isEqualTo(LocalDate.of(2026, 11, 10));
            assertThat(response.organizerId()).isEqualTo(1);
            assertThat(response.organizerUsername()).isEqualTo("organizer");
        }
    }

    // ------------------------------------------------------------------
    // getAll
    // ------------------------------------------------------------------

    @Nested
    @DisplayName("getAll : lister les tournois, avec ou sans filtre de statut")
    class GetAll {

        @Test
        @DisplayName("sans statut : tous les tournois, sans passer par le filtre")
        void withoutStatus() {
            when(tournamentRepository.findAll()).thenReturn(List.of(
                    tournament(TournamentStatus.UPCOMING),
                    tournament(TournamentStatus.FINISHED)));

            List<TournamentResponse> result = service.getAll(null);

            assertThat(result).extracting(TournamentResponse::status)
                    .containsExactly(TournamentStatus.UPCOMING, TournamentStatus.FINISHED);
            verify(tournamentRepository, never()).findByStatus(any(TournamentStatus.class));
        }

        @Test
        @DisplayName("avec un statut : seuls les tournois de ce statut sont demandés")
        void withStatus() {
            when(tournamentRepository.findByStatus(TournamentStatus.UPCOMING))
                    .thenReturn(List.of(tournament(TournamentStatus.UPCOMING)));

            List<TournamentResponse> result = service.getAll(TournamentStatus.UPCOMING);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).status()).isEqualTo(TournamentStatus.UPCOMING);
            verify(tournamentRepository, never()).findAll();
        }

        @Test
        @DisplayName("aucun tournoi : liste vide")
        void noTournament() {
            when(tournamentRepository.findByStatus(TournamentStatus.FINISHED)).thenReturn(List.of());

            assertThat(service.getAll(TournamentStatus.FINISHED)).isEmpty();
        }
    }

    // ------------------------------------------------------------------
    // update
    // ------------------------------------------------------------------

    @Nested
    @DisplayName("update : modifier un tournoi")
    class Update {

        @Test
        @DisplayName("tournoi inexistant : TournamentNotFoundException")
        void notFound() {
            when(tournamentRepository.findById(99)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.update(99, updateRequest(8, ParticipantType.PLAYER)))
                    .isInstanceOf(TournamentNotFoundException.class);

            verifyNoInteractions(registerUserRepository, registerTeamRepository);
        }

        @ParameterizedTest
        @EnumSource(value = TournamentStatus.class, names = {"IN_PROGRESS", "FINISHED", "CANCELED"})
        @DisplayName("tournoi commencé, terminé ou annulé : refusé, rien n'est enregistré")
        void notModifiable(TournamentStatus status) {
            when(tournamentRepository.findById(TOURNAMENT_ID)).thenReturn(Optional.of(tournament(status)));

            assertThatThrownBy(() -> service.update(TOURNAMENT_ID, updateRequest(8, ParticipantType.PLAYER)))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("ne peut plus être modifié");

            verify(tournamentRepository, never()).save(any(TournamentEntity.class));
            verifyNoInteractions(registerUserRepository, registerTeamRepository);
        }

        @Test
        @DisplayName("dates incohérentes : refusé avant tout comptage d'inscriptions")
        void inconsistentDates() {
            when(tournamentRepository.findById(TOURNAMENT_ID))
                    .thenReturn(Optional.of(tournament(TournamentStatus.UPCOMING)));
            TournamentUpdateRequest request = updateRequest(8, ParticipantType.PLAYER,
                    LocalDate.of(2026, 12, 2), LocalDate.of(2026, 12, 1),
                    LocalDate.of(2026, 11, 1), LocalDate.of(2026, 11, 25));

            assertThatThrownBy(() -> service.update(TOURNAMENT_ID, request))
                    .isInstanceOf(IllegalArgumentException.class);

            verify(tournamentRepository, never()).save(any(TournamentEntity.class));
            verifyNoInteractions(registerUserRepository, registerTeamRepository);
        }

        @Test
        @DisplayName("changement de type avec des inscriptions de joueurs : refusé")
        void typeChangeWithPlayerRegistrations() {
            when(tournamentRepository.findById(TOURNAMENT_ID))
                    .thenReturn(Optional.of(tournament(TournamentStatus.REGISTRATION_OPEN)));
            when(registerUserRepository.countByTournamentId(TOURNAMENT_ID)).thenReturn(1L);

            assertThatThrownBy(() -> service.update(TOURNAMENT_ID, updateRequest(8, ParticipantType.TEAM)))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("type de participant");

            verify(tournamentRepository, never()).save(any(TournamentEntity.class));
        }

        @Test
        @DisplayName("changement de type avec des inscriptions d'équipes : refusé")
        void typeChangeWithTeamRegistrations() {
            when(tournamentRepository.findById(TOURNAMENT_ID))
                    .thenReturn(Optional.of(tournament(TournamentStatus.REGISTRATION_OPEN)));
            when(registerTeamRepository.countByTournamentId(TOURNAMENT_ID)).thenReturn(2L);

            assertThatThrownBy(() -> service.update(TOURNAMENT_ID, updateRequest(8, ParticipantType.TEAM)))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("type de participant");

            verify(tournamentRepository, never()).save(any(TournamentEntity.class));
        }

        @Test
        @DisplayName("changement de type sans aucune inscription : accepté")
        void typeChangeWithoutRegistrations() {
            TournamentEntity tournament = tournament(TournamentStatus.UPCOMING);
            when(tournamentRepository.findById(TOURNAMENT_ID)).thenReturn(Optional.of(tournament));
            echoSave();

            TournamentResponse response = service.update(TOURNAMENT_ID, updateRequest(8, ParticipantType.TEAM));

            assertThat(response.participantType()).isEqualTo(ParticipantType.TEAM);
            assertThat(tournament.getParticipantType()).isEqualTo(ParticipantType.TEAM);
        }

        @Test
        @DisplayName("même type avec des inscriptions : accepté, sans même compter les inscriptions")
        void sameTypeWithRegistrations() {
            when(tournamentRepository.findById(TOURNAMENT_ID))
                    .thenReturn(Optional.of(tournament(TournamentStatus.REGISTRATION_OPEN)));
            echoSave();

            TournamentResponse response = service.update(TOURNAMENT_ID, updateRequest(8, ParticipantType.PLAYER));

            assertThat(response.name()).isEqualTo("Nouveau nom");
            verify(registerUserRepository, never()).countByTournamentId(any());
            verify(registerTeamRepository, never()).countByTournamentId(any());
        }

        @Test
        @DisplayName("maximum inférieur au nombre de joueurs validés : refusé")
        void maxBelowValidatedPlayers() {
            when(tournamentRepository.findById(TOURNAMENT_ID))
                    .thenReturn(Optional.of(tournament(TournamentStatus.REGISTRATION_OPEN)));
            when(registerUserRepository.countByTournamentIdAndStatus(TOURNAMENT_ID, RegistrationStatus.VALIDATED))
                    .thenReturn(3L);

            assertThatThrownBy(() -> service.update(TOURNAMENT_ID, updateRequest(2, ParticipantType.PLAYER)))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("inférieur");

            verify(tournamentRepository, never()).save(any(TournamentEntity.class));
        }

        @Test
        @DisplayName("maximum inférieur au nombre d'équipes validées : refusé")
        void maxBelowValidatedTeams() {
            when(tournamentRepository.findById(TOURNAMENT_ID))
                    .thenReturn(Optional.of(tournament(TournamentStatus.REGISTRATION_OPEN)));
            when(registerTeamRepository.countByTournamentIdAndStatus(TOURNAMENT_ID, RegistrationStatus.VALIDATED))
                    .thenReturn(3L);

            assertThatThrownBy(() -> service.update(TOURNAMENT_ID, updateRequest(2, ParticipantType.PLAYER)))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("inférieur");
        }

        @Test
        @DisplayName("les validés des deux tables sont additionnés")
        void validatedOfBothTablesAreAdded() {
            when(tournamentRepository.findById(TOURNAMENT_ID))
                    .thenReturn(Optional.of(tournament(TournamentStatus.REGISTRATION_OPEN)));
            when(registerUserRepository.countByTournamentIdAndStatus(TOURNAMENT_ID, RegistrationStatus.VALIDATED))
                    .thenReturn(1L);
            when(registerTeamRepository.countByTournamentIdAndStatus(TOURNAMENT_ID, RegistrationStatus.VALIDATED))
                    .thenReturn(2L);

            assertThatThrownBy(() -> service.update(TOURNAMENT_ID, updateRequest(2, ParticipantType.PLAYER)))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("inférieur");
        }

        @Test
        @DisplayName("maximum égal au nombre de validés : accepté")
        void maxEqualToValidated() {
            TournamentEntity tournament = tournament(TournamentStatus.REGISTRATION_OPEN);
            when(tournamentRepository.findById(TOURNAMENT_ID)).thenReturn(Optional.of(tournament));
            when(registerUserRepository.countByTournamentIdAndStatus(TOURNAMENT_ID, RegistrationStatus.VALIDATED))
                    .thenReturn(3L);
            echoSave();

            TournamentResponse response = service.update(TOURNAMENT_ID, updateRequest(3, ParticipantType.PLAYER));

            assertThat(response.maxParticipants()).isEqualTo(3);
        }

        @ParameterizedTest
        @EnumSource(value = TournamentStatus.class,
                names = {"UPCOMING", "REGISTRATION_OPEN", "REGISTRATION_CLOSED"})
        @DisplayName("tournoi pas encore commencé : toutes les informations sont modifiées, l'organisateur et le statut restent")
        void validUpdate(TournamentStatus status) {
            UserEntity organizer = user(1, "organizer");
            TournamentEntity tournament = tournament(status, organizer);
            when(tournamentRepository.findById(TOURNAMENT_ID)).thenReturn(Optional.of(tournament));
            echoSave();

            TournamentResponse response = service.update(TOURNAMENT_ID, updateRequest(10, ParticipantType.PLAYER));

            assertThat(tournament.getName()).isEqualTo("Nouveau nom");
            assertThat(tournament.getDescription()).isEqualTo("Nouvelle description");
            assertThat(tournament.getMaxParticipants()).isEqualTo(10);
            assertThat(tournament.getFormat()).isEqualTo(TournamentFormat.GROUPS_THEN_PLAYOFF);
            assertThat(tournament.getParticipantType()).isEqualTo(ParticipantType.PLAYER);
            assertThat(tournament.getStartDate()).isEqualTo(LocalDate.of(2026, 12, 1));
            assertThat(tournament.getEndDate()).isEqualTo(LocalDate.of(2026, 12, 2));
            assertThat(tournament.getRegistrationStartDate()).isEqualTo(LocalDate.of(2026, 11, 1));
            assertThat(tournament.getRegistrationEndDate()).isEqualTo(LocalDate.of(2026, 11, 25));
            assertThat(tournament.getOrganizer()).isSameAs(organizer);
            assertThat(tournament.getStatus()).isEqualTo(status);

            assertThat(response.name()).isEqualTo("Nouveau nom");
            assertThat(response.status()).isEqualTo(status);
            verify(tournamentRepository).save(tournament);
        }
    }

    // ------------------------------------------------------------------
    // cancel
    // ------------------------------------------------------------------

    @Nested
    @DisplayName("cancel : annuler un tournoi")
    class Cancel {

        @Test
        @DisplayName("tournoi inexistant : TournamentNotFoundException")
        void notFound() {
            when(tournamentRepository.findById(99)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.cancel(99))
                    .isInstanceOf(TournamentNotFoundException.class);
        }

        @ParameterizedTest
        @EnumSource(value = TournamentStatus.class, names = {"IN_PROGRESS", "FINISHED"})
        @DisplayName("tournoi commencé ou terminé : refusé, le statut ne change pas")
        void alreadyStarted(TournamentStatus status) {
            TournamentEntity tournament = tournament(status);
            when(tournamentRepository.findById(TOURNAMENT_ID)).thenReturn(Optional.of(tournament));

            assertThatThrownBy(() -> service.cancel(TOURNAMENT_ID))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("commencé");

            assertThat(tournament.getStatus()).isEqualTo(status);
            verify(tournamentRepository, never()).save(any(TournamentEntity.class));
        }

        @Test
        @DisplayName("tournoi déjà annulé : refusé")
        void alreadyCanceled() {
            when(tournamentRepository.findById(TOURNAMENT_ID))
                    .thenReturn(Optional.of(tournament(TournamentStatus.CANCELED)));

            assertThatThrownBy(() -> service.cancel(TOURNAMENT_ID))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("déjà annulé");

            verify(tournamentRepository, never()).save(any(TournamentEntity.class));
        }

        @ParameterizedTest
        @EnumSource(value = TournamentStatus.class,
                names = {"UPCOMING", "REGISTRATION_OPEN", "REGISTRATION_CLOSED"})
        @DisplayName("tournoi pas encore commencé : passe en CANCELED")
        void cancelable(TournamentStatus status) {
            TournamentEntity tournament = tournament(status);
            when(tournamentRepository.findById(TOURNAMENT_ID)).thenReturn(Optional.of(tournament));
            echoSave();

            TournamentResponse response = service.cancel(TOURNAMENT_ID);

            assertThat(response.status()).isEqualTo(TournamentStatus.CANCELED);
            assertThat(tournament.getStatus()).isEqualTo(TournamentStatus.CANCELED);
            verify(tournamentRepository).save(tournament);
        }
    }

    // ------------------------------------------------------------------
    // changeStatus
    // ------------------------------------------------------------------

    @Nested
    @DisplayName("changeStatus : changer le statut en respectant les transitions autorisées")
    class ChangeStatus {

        @Test
        @DisplayName("tournoi inexistant : TournamentNotFoundException")
        void notFound() {
            when(tournamentRepository.findById(99)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.changeStatus(99, TournamentStatus.REGISTRATION_OPEN))
                    .isInstanceOf(TournamentNotFoundException.class);
        }

        @ParameterizedTest(name = "{0} vers {1} : autorisé")
        @CsvSource({
                "UPCOMING, REGISTRATION_OPEN",
                "UPCOMING, CANCELED",
                "REGISTRATION_OPEN, REGISTRATION_CLOSED",
                "REGISTRATION_OPEN, CANCELED",
                "REGISTRATION_CLOSED, REGISTRATION_OPEN",
                "REGISTRATION_CLOSED, IN_PROGRESS",
                "REGISTRATION_CLOSED, CANCELED",
                "IN_PROGRESS, FINISHED"
        })
        @DisplayName("transitions autorisées")
        void allowedTransition(TournamentStatus from, TournamentStatus to) {
            TournamentEntity tournament = tournament(from);
            when(tournamentRepository.findById(TOURNAMENT_ID)).thenReturn(Optional.of(tournament));
            echoSave();

            TournamentResponse response = service.changeStatus(TOURNAMENT_ID, to);

            assertThat(response.status()).isEqualTo(to);
            assertThat(tournament.getStatus()).isEqualTo(to);
            verify(tournamentRepository).save(tournament);
        }

        @ParameterizedTest(name = "{0} vers {1} : refusé")
        @CsvSource({
                "UPCOMING, UPCOMING",
                "UPCOMING, REGISTRATION_CLOSED",
                "UPCOMING, IN_PROGRESS",
                "UPCOMING, FINISHED",
                "REGISTRATION_OPEN, UPCOMING",
                "REGISTRATION_OPEN, REGISTRATION_OPEN",
                "REGISTRATION_OPEN, IN_PROGRESS",
                "REGISTRATION_OPEN, FINISHED",
                "REGISTRATION_CLOSED, UPCOMING",
                "REGISTRATION_CLOSED, REGISTRATION_CLOSED",
                "REGISTRATION_CLOSED, FINISHED",
                "IN_PROGRESS, UPCOMING",
                "IN_PROGRESS, REGISTRATION_OPEN",
                "IN_PROGRESS, REGISTRATION_CLOSED",
                "IN_PROGRESS, CANCELED",
                "FINISHED, UPCOMING",
                "FINISHED, REGISTRATION_OPEN",
                "FINISHED, IN_PROGRESS",
                "FINISHED, CANCELED",
                "CANCELED, UPCOMING",
                "CANCELED, REGISTRATION_OPEN",
                "CANCELED, IN_PROGRESS",
                "CANCELED, FINISHED"
        })
        @DisplayName("transitions interdites")
        void forbiddenTransition(TournamentStatus from, TournamentStatus to) {
            TournamentEntity tournament = tournament(from);
            when(tournamentRepository.findById(TOURNAMENT_ID)).thenReturn(Optional.of(tournament));

            assertThatThrownBy(() -> service.changeStatus(TOURNAMENT_ID, to))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("impossible");

            assertThat(tournament.getStatus()).isEqualTo(from);
            verify(tournamentRepository, never()).save(any(TournamentEntity.class));
        }
    }

    // ------------------------------------------------------------------
    // getDetail
    // ------------------------------------------------------------------

    @Nested
    @DisplayName("getDetail : tournoi avec ses participants")
    class GetDetail {

        @Test
        @DisplayName("tournoi inexistant : TournamentNotFoundException, les participants ne sont pas demandés")
        void notFound() {
            when(tournamentRepository.findById(99)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.getDetail(99))
                    .isInstanceOf(TournamentNotFoundException.class);

            verifyNoInteractions(participantService);
        }

        @Test
        @DisplayName("tournoi existant : informations du tournoi, comptage et liste des participants")
        void found() {
            ParticipantCountResponse count = new ParticipantCountResponse(TOURNAMENT_ID, 1L, 8, 7L);
            ParticipantResponse participant = new ParticipantResponse(10, TOURNAMENT_ID, ParticipantType.PLAYER,
                    2, "alice", "alice@test.com", null, null,
                    LocalDateTime.of(2026, 10, 1, 10, 30), RegistrationStatus.VALIDATED);
            when(tournamentRepository.findById(TOURNAMENT_ID))
                    .thenReturn(Optional.of(tournament(TournamentStatus.REGISTRATION_OPEN)));
            when(participantService.count(TOURNAMENT_ID)).thenReturn(count);
            when(participantService.getByTournament(TOURNAMENT_ID)).thenReturn(List.of(participant));

            TournamentDetailResponse detail = service.getDetail(TOURNAMENT_ID);

            assertThat(detail.tournament().id()).isEqualTo(TOURNAMENT_ID);
            assertThat(detail.tournament().name()).isEqualTo("Tournoi de printemps");
            assertThat(detail.participantCount()).isSameAs(count);
            assertThat(detail.participants()).containsExactly(participant);
        }
    }

    // ------------------------------------------------------------------
    // changeOrganizer
    // ------------------------------------------------------------------

    @Nested
    @DisplayName("changeOrganizer : transférer l'organisation d'un tournoi")
    class ChangeOrganizer {

        @Test
        @DisplayName("tournoi inexistant : TournamentNotFoundException")
        void tournamentNotFound() {
            when(tournamentRepository.findById(99)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.changeOrganizer(99, 2))
                    .isInstanceOf(TournamentNotFoundException.class);

            verifyNoInteractions(userRepository);
        }

        @ParameterizedTest
        @EnumSource(value = TournamentStatus.class, names = {"FINISHED", "CANCELED"})
        @DisplayName("tournoi terminé ou annulé : refusé, l'organisateur ne change pas")
        void finishedOrCanceled(TournamentStatus status) {
            UserEntity organizer = user(1, "organizer");
            TournamentEntity tournament = tournament(status, organizer);
            when(tournamentRepository.findById(TOURNAMENT_ID)).thenReturn(Optional.of(tournament));

            assertThatThrownBy(() -> service.changeOrganizer(TOURNAMENT_ID, 2))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("terminé ou annulé");

            assertThat(tournament.getOrganizer()).isSameAs(organizer);
            verifyNoInteractions(userRepository);
            verify(tournamentRepository, never()).save(any(TournamentEntity.class));
        }

        @Test
        @DisplayName("même organisateur : refusé")
        void sameOrganizer() {
            when(tournamentRepository.findById(TOURNAMENT_ID))
                    .thenReturn(Optional.of(tournament(TournamentStatus.UPCOMING, user(1, "organizer"))));

            assertThatThrownBy(() -> service.changeOrganizer(TOURNAMENT_ID, 1))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("déjà l'organisateur");

            verifyNoInteractions(userRepository);
            verify(tournamentRepository, never()).save(any(TournamentEntity.class));
        }

        @Test
        @DisplayName("même organisateur avec un id supérieur à 127 : comparé par valeur, pas par référence")
        void sameOrganizerWithLargeId() {
            when(tournamentRepository.findById(TOURNAMENT_ID)).thenReturn(Optional.of(
                    tournament(TournamentStatus.UPCOMING, user(Integer.valueOf(1000), "organizer"))));

            assertThatThrownBy(() -> service.changeOrganizer(TOURNAMENT_ID, Integer.valueOf(1000)))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("déjà l'organisateur");

            verify(tournamentRepository, never()).save(any(TournamentEntity.class));
        }

        @Test
        @DisplayName("nouvel organisateur inexistant : UserNotFoundException, l'organisateur ne change pas")
        void newOrganizerNotFound() {
            UserEntity organizer = user(1, "organizer");
            TournamentEntity tournament = tournament(TournamentStatus.UPCOMING, organizer);
            when(tournamentRepository.findById(TOURNAMENT_ID)).thenReturn(Optional.of(tournament));
            when(userRepository.findById(99)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.changeOrganizer(TOURNAMENT_ID, 99))
                    .isInstanceOf(UserNotFoundException.class);

            assertThat(tournament.getOrganizer()).isSameAs(organizer);
            verify(tournamentRepository, never()).save(any(TournamentEntity.class));
        }

        @ParameterizedTest
        @EnumSource(value = TournamentStatus.class, names = {"FINISHED", "CANCELED"}, mode = EnumSource.Mode.EXCLUDE)
        @DisplayName("tournoi ni terminé ni annulé (même en cours) : le nouvel organisateur est enregistré")
        void validTransfer(TournamentStatus status) {
            TournamentEntity tournament = tournament(status, user(1, "organizer"));
            UserEntity newOrganizer = user(2, "alice");
            when(tournamentRepository.findById(TOURNAMENT_ID)).thenReturn(Optional.of(tournament));
            when(userRepository.findById(2)).thenReturn(Optional.of(newOrganizer));
            echoSave();

            TournamentResponse response = service.changeOrganizer(TOURNAMENT_ID, 2);

            assertThat(tournament.getOrganizer()).isSameAs(newOrganizer);
            assertThat(tournament.getStatus()).isEqualTo(status);
            assertThat(response.organizerId()).isEqualTo(2);
            assertThat(response.organizerUsername()).isEqualTo("alice");
            verify(tournamentRepository).save(tournament);
        }
    }
}