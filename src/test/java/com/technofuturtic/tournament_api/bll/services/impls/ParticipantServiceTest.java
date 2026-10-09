package com.technofuturtic.tournament_api.bll.services.impls;

import com.technofuturtic.tournament_api.api.models.tournament.responses.ParticipantCountResponse;
import com.technofuturtic.tournament_api.api.models.tournament.responses.ParticipantResponse;
import com.technofuturtic.tournament_api.bll.exceptions.participant.ParticipantNotFoundException;
import com.technofuturtic.tournament_api.bll.exceptions.registration.RegistrationNotFoundException;
import com.technofuturtic.tournament_api.bll.exceptions.team.TeamNotFoundException;
import com.technofuturtic.tournament_api.bll.exceptions.tournament.TournamentNotFoundException;
import com.technofuturtic.tournament_api.dal.repositories.ParticipantRepository;
import com.technofuturtic.tournament_api.dal.repositories.RegisterTeamRepository;
import com.technofuturtic.tournament_api.dal.repositories.RegisterUserRepository;
import com.technofuturtic.tournament_api.dal.repositories.TeamRepository;
import com.technofuturtic.tournament_api.dal.repositories.TournamentRepository;
import com.technofuturtic.tournament_api.dl.entities.ParticipantEntity;
import com.technofuturtic.tournament_api.dl.entities.RegisterTeamEntity;
import com.technofuturtic.tournament_api.dl.entities.RegisterUserEntity;
import com.technofuturtic.tournament_api.dl.entities.TeamEntity;
import com.technofuturtic.tournament_api.dl.entities.TournamentEntity;
import com.technofuturtic.tournament_api.dl.entities.UserEntity;
import com.technofuturtic.tournament_api.dl.enums.ParticipantType;
import com.technofuturtic.tournament_api.dl.enums.RegistrationStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ParticipantServiceTest {

    @Mock private ParticipantRepository participantRepository;
    @Mock private TournamentRepository tournamentRepository;
    @Mock private RegisterUserRepository registerUserRepository;
    @Mock private RegisterTeamRepository registerTeamRepository;
    @Mock private TeamRepository teamRepository;

    @InjectMocks
    private ParticipantServiceImpl service;

    private static final LocalDateTime REGISTER_DATE = LocalDateTime.of(2026, 10, 1, 10, 30);

    // ------------------------------------------------------------------
    // Méthodes utilitaires de construction des données de test.
    // Les entités n'ont pas de setter sur l'id : on le renseigne par réflexion.
    // ------------------------------------------------------------------

    private static <T> T withId(T entity, Integer id) {
        ReflectionTestUtils.setField(entity, "id", id);
        return entity;
    }

    private TournamentEntity tournament(Integer id, ParticipantType type, int maxParticipants) {
        TournamentEntity tournament = new TournamentEntity();
        tournament.setParticipantType(type);
        tournament.setMaxParticipants(maxParticipants);
        return withId(tournament, id);
    }

    private UserEntity user(Integer id, String username, String email) {
        UserEntity user = new UserEntity();
        user.setUsername(username);
        user.setEmail(email);
        return withId(user, id);
    }

    private TeamEntity team(Integer id, String name) {
        TeamEntity team = new TeamEntity();
        team.setName(name);
        return withId(team, id);
    }

    private ParticipantEntity participantOfUser(Integer id, TournamentEntity tournament, UserEntity user) {
        ParticipantEntity participant = new ParticipantEntity();
        participant.setTournament(tournament);
        participant.setUser(user);
        return withId(participant, id);
    }

    private ParticipantEntity participantOfTeam(Integer id, TournamentEntity tournament, TeamEntity team) {
        ParticipantEntity participant = new ParticipantEntity();
        participant.setTournament(tournament);
        participant.setTeam(team);
        return withId(participant, id);
    }

    private RegisterUserEntity userRegistration(UserEntity user, TournamentEntity tournament) {
        RegisterUserEntity registration = new RegisterUserEntity();
        registration.setUser(user);
        registration.setTournament(tournament);
        registration.setRegisterDate(REGISTER_DATE);
        registration.setStatus(RegistrationStatus.VALIDATED);
        return registration;
    }

    private RegisterTeamEntity teamRegistration(TeamEntity team, TournamentEntity tournament) {
        RegisterTeamEntity registration = new RegisterTeamEntity();
        registration.setTeam(team);
        registration.setTournament(tournament);
        registration.setRegisterDate(REGISTER_DATE);
        registration.setStatus(RegistrationStatus.VALIDATED);
        return registration;
    }

    // ------------------------------------------------------------------
    // getByTournament
    // ------------------------------------------------------------------

    @Nested
    @DisplayName("getByTournament : liste des participants d'un tournoi")
    class GetByTournament {

        @Test
        @DisplayName("tournoi inexistant : TournamentNotFoundException, aucune lecture des participants")
        void tournamentNotFound() {
            when(tournamentRepository.existsById(99)).thenReturn(false);

            assertThatThrownBy(() -> service.getByTournament(99))
                    .isInstanceOf(TournamentNotFoundException.class);

            verifyNoInteractions(participantRepository);
        }

        @Test
        @DisplayName("tournoi sans participant : liste vide")
        void noParticipants() {
            when(tournamentRepository.existsById(1)).thenReturn(true);
            when(participantRepository.findByTournamentId(1)).thenReturn(List.of());

            assertThat(service.getByTournament(1)).isEmpty();
        }

        @Test
        @DisplayName("tournoi de joueurs : username, email, date et statut de l'inscription")
        void playerParticipants() {
            TournamentEntity tournament = tournament(1, ParticipantType.PLAYER, 4);
            UserEntity alice = user(2, "alice", "alice@test.com");
            UserEntity bob = user(3, "bob", "bob@test.com");

            when(tournamentRepository.existsById(1)).thenReturn(true);
            when(participantRepository.findByTournamentId(1)).thenReturn(List.of(
                    participantOfUser(10, tournament, alice),
                    participantOfUser(11, tournament, bob)));
            when(registerUserRepository.findByUserIdAndTournamentId(2, 1))
                    .thenReturn(Optional.of(userRegistration(alice, tournament)));
            when(registerUserRepository.findByUserIdAndTournamentId(3, 1))
                    .thenReturn(Optional.of(userRegistration(bob, tournament)));

            List<ParticipantResponse> result = service.getByTournament(1);

            assertThat(result).hasSize(2);

            ParticipantResponse first = result.get(0);
            assertThat(first.id()).isEqualTo(10);
            assertThat(first.tournamentId()).isEqualTo(1);
            assertThat(first.type()).isEqualTo(ParticipantType.PLAYER);
            assertThat(first.userId()).isEqualTo(2);
            assertThat(first.username()).isEqualTo("alice");
            assertThat(first.email()).isEqualTo("alice@test.com");
            assertThat(first.teamId()).isNull();
            assertThat(first.teamName()).isNull();
            assertThat(first.registerDate()).isEqualTo(REGISTER_DATE);
            assertThat(first.status()).isEqualTo(RegistrationStatus.VALIDATED);

            assertThat(result.get(1).username()).isEqualTo("bob");
        }

        @Test
        @DisplayName("tournoi d'équipes : teamId et teamName renseignés, pas d'email")
        void teamParticipants() {
            TournamentEntity tournament = tournament(1, ParticipantType.TEAM, 8);
            TeamEntity lions = team(5, "Les Lions");

            when(tournamentRepository.existsById(1)).thenReturn(true);
            when(participantRepository.findByTournamentId(1)).thenReturn(List.of(
                    participantOfTeam(20, tournament, lions)));
            when(registerTeamRepository.findByTeamIdAndTournamentId(5, 1))
                    .thenReturn(Optional.of(teamRegistration(lions, tournament)));

            List<ParticipantResponse> result = service.getByTournament(1);

            assertThat(result).hasSize(1);
            ParticipantResponse response = result.get(0);
            assertThat(response.id()).isEqualTo(20);
            assertThat(response.type()).isEqualTo(ParticipantType.TEAM);
            assertThat(response.teamId()).isEqualTo(5);
            assertThat(response.teamName()).isEqualTo("Les Lions");
            assertThat(response.userId()).isNull();
            assertThat(response.username()).isNull();
            assertThat(response.email()).isNull();
            assertThat(response.registerDate()).isEqualTo(REGISTER_DATE);
            assertThat(response.status()).isEqualTo(RegistrationStatus.VALIDATED);
        }

        @Test
        @DisplayName("participant sans inscription correspondante : RegistrationNotFoundException")
        void participantWithoutRegistration() {
            TournamentEntity tournament = tournament(1, ParticipantType.PLAYER, 4);
            UserEntity alice = user(2, "alice", "alice@test.com");

            when(tournamentRepository.existsById(1)).thenReturn(true);
            when(participantRepository.findByTournamentId(1)).thenReturn(List.of(
                    participantOfUser(10, tournament, alice)));
            when(registerUserRepository.findByUserIdAndTournamentId(2, 1)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.getByTournament(1))
                    .isInstanceOf(RegistrationNotFoundException.class);
        }
    }

    // ------------------------------------------------------------------
    // getById
    // ------------------------------------------------------------------

    @Nested
    @DisplayName("getById : un participant d'un tournoi")
    class GetById {

        @Test
        @DisplayName("tournoi inexistant : TournamentNotFoundException, aucune recherche du participant")
        void tournamentNotFound() {
            when(tournamentRepository.existsById(99)).thenReturn(false);

            assertThatThrownBy(() -> service.getById(99, 10))
                    .isInstanceOf(TournamentNotFoundException.class);

            verifyNoInteractions(participantRepository);
        }

        @Test
        @DisplayName("participant inexistant : ParticipantNotFoundException")
        void participantNotFound() {
            when(tournamentRepository.existsById(1)).thenReturn(true);
            when(participantRepository.findByIdAndTournamentId(50, 1)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.getById(1, 50))
                    .isInstanceOf(ParticipantNotFoundException.class);
        }

        @Test
        @DisplayName("cherche le participant dans le tournoi demandé (pas dans un autre)")
        void searchesInRequestedTournament() {
            when(tournamentRepository.existsById(2)).thenReturn(true);
            when(participantRepository.findByIdAndTournamentId(10, 2)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.getById(2, 10))
                    .isInstanceOf(ParticipantNotFoundException.class);

            verify(participantRepository).findByIdAndTournamentId(10, 2);
        }

        @Test
        @DisplayName("participant joueur : informations du joueur et de l'inscription")
        void playerParticipant() {
            TournamentEntity tournament = tournament(1, ParticipantType.PLAYER, 4);
            UserEntity alice = user(2, "alice", "alice@test.com");

            when(tournamentRepository.existsById(1)).thenReturn(true);
            when(participantRepository.findByIdAndTournamentId(10, 1))
                    .thenReturn(Optional.of(participantOfUser(10, tournament, alice)));
            when(registerUserRepository.findByUserIdAndTournamentId(2, 1))
                    .thenReturn(Optional.of(userRegistration(alice, tournament)));

            ParticipantResponse response = service.getById(1, 10);

            assertThat(response.id()).isEqualTo(10);
            assertThat(response.type()).isEqualTo(ParticipantType.PLAYER);
            assertThat(response.username()).isEqualTo("alice");
            assertThat(response.email()).isEqualTo("alice@test.com");
            assertThat(response.registerDate()).isEqualTo(REGISTER_DATE);
            assertThat(response.status()).isEqualTo(RegistrationStatus.VALIDATED);
        }

        @Test
        @DisplayName("participant équipe : informations de l'équipe et de l'inscription")
        void teamParticipant() {
            TournamentEntity tournament = tournament(1, ParticipantType.TEAM, 8);
            TeamEntity lions = team(5, "Les Lions");

            when(tournamentRepository.existsById(1)).thenReturn(true);
            when(participantRepository.findByIdAndTournamentId(20, 1))
                    .thenReturn(Optional.of(participantOfTeam(20, tournament, lions)));
            when(registerTeamRepository.findByTeamIdAndTournamentId(5, 1))
                    .thenReturn(Optional.of(teamRegistration(lions, tournament)));

            ParticipantResponse response = service.getById(1, 20);

            assertThat(response.id()).isEqualTo(20);
            assertThat(response.type()).isEqualTo(ParticipantType.TEAM);
            assertThat(response.teamId()).isEqualTo(5);
            assertThat(response.teamName()).isEqualTo("Les Lions");
            assertThat(response.email()).isNull();
        }
    }

    // ------------------------------------------------------------------
    // getByTournamentIdAndTeamId
    // ------------------------------------------------------------------

    @Nested
    @DisplayName("getByTournamentIdAndTeamId : participant d'une équipe dans un tournoi")
    class GetByTournamentIdAndTeamId {

        @Test
        @DisplayName("tournoi inexistant : TournamentNotFoundException")
        void tournamentNotFound() {
            when(tournamentRepository.existsById(99)).thenReturn(false);

            assertThatThrownBy(() -> service.getByTournamentIdAndTeamId(99, 5))
                    .isInstanceOf(TournamentNotFoundException.class);

            verifyNoInteractions(teamRepository, participantRepository);
        }

        @Test
        @DisplayName("équipe inexistante : TeamNotFoundException")
        void teamNotFound() {
            when(tournamentRepository.existsById(1)).thenReturn(true);
            when(teamRepository.existsById(99)).thenReturn(false);

            assertThatThrownBy(() -> service.getByTournamentIdAndTeamId(1, 99))
                    .isInstanceOf(TeamNotFoundException.class);

            verifyNoInteractions(participantRepository);
        }

        @Test
        @DisplayName("équipe participante : une réponse avec les informations de l'équipe")
        void teamIsParticipant() {
            TournamentEntity tournament = tournament(1, ParticipantType.TEAM, 8);
            TeamEntity lions = team(5, "Les Lions");

            when(tournamentRepository.existsById(1)).thenReturn(true);
            when(teamRepository.existsById(5)).thenReturn(true);
            when(participantRepository.findByTeamIdAndTournamentId(5, 1))
                    .thenReturn(Optional.of(participantOfTeam(20, tournament, lions)));
            when(registerTeamRepository.findByTeamIdAndTournamentId(5, 1))
                    .thenReturn(Optional.of(teamRegistration(lions, tournament)));

            List<ParticipantResponse> result = service.getByTournamentIdAndTeamId(1, 5);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).teamId()).isEqualTo(5);
            assertThat(result.get(0).teamName()).isEqualTo("Les Lions");
        }

        @Test
        @DisplayName("équipe qui n'est pas participante : liste vide")
        void teamIsNotParticipant() {
            when(tournamentRepository.existsById(1)).thenReturn(true);
            when(teamRepository.existsById(5)).thenReturn(true);
            when(participantRepository.findByTeamIdAndTournamentId(5, 1)).thenReturn(Optional.empty());

            assertThat(service.getByTournamentIdAndTeamId(1, 5)).isEmpty();
        }
    }

    // ------------------------------------------------------------------
    // count
    // ------------------------------------------------------------------

    @Nested
    @DisplayName("count : nombre de participants, maximum et places restantes")
    class Count {

        @Test
        @DisplayName("tournoi inexistant : TournamentNotFoundException, aucun comptage")
        void tournamentNotFound() {
            when(tournamentRepository.findById(99)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.count(99))
                    .isInstanceOf(TournamentNotFoundException.class);

            verifyNoInteractions(registerUserRepository, registerTeamRepository);
        }

        @Test
        @DisplayName("tournoi de joueurs : compte les joueurs VALIDATED, pas les équipes")
        void playerTournament() {
            when(tournamentRepository.findById(1))
                    .thenReturn(Optional.of(tournament(1, ParticipantType.PLAYER, 4)));
            when(registerUserRepository.countByTournamentIdAndStatus(1, RegistrationStatus.VALIDATED))
                    .thenReturn(2L);

            ParticipantCountResponse response = service.count(1);

            assertThat(response.tournamentId()).isEqualTo(1);
            assertThat(response.current()).isEqualTo(2L);
            assertThat(response.max()).isEqualTo(4);
            assertThat(response.remaining()).isEqualTo(2L);
            verifyNoInteractions(registerTeamRepository);
        }

        @Test
        @DisplayName("tournoi d'équipes : compte les équipes VALIDATED, pas les joueurs")
        void teamTournament() {
            when(tournamentRepository.findById(1))
                    .thenReturn(Optional.of(tournament(1, ParticipantType.TEAM, 8)));
            when(registerTeamRepository.countByTournamentIdAndStatus(1, RegistrationStatus.VALIDATED))
                    .thenReturn(3L);

            ParticipantCountResponse response = service.count(1);

            assertThat(response.current()).isEqualTo(3L);
            assertThat(response.max()).isEqualTo(8);
            assertThat(response.remaining()).isEqualTo(5L);
            verifyNoInteractions(registerUserRepository);
        }

        @Test
        @DisplayName("aucun participant validé : toutes les places restent")
        void noValidatedParticipant() {
            when(tournamentRepository.findById(1))
                    .thenReturn(Optional.of(tournament(1, ParticipantType.PLAYER, 4)));
            when(registerUserRepository.countByTournamentIdAndStatus(1, RegistrationStatus.VALIDATED))
                    .thenReturn(0L);

            ParticipantCountResponse response = service.count(1);

            assertThat(response.current()).isZero();
            assertThat(response.remaining()).isEqualTo(4L);
        }

        @Test
        @DisplayName("tournoi complet : zéro place restante")
        void fullTournament() {
            when(tournamentRepository.findById(1))
                    .thenReturn(Optional.of(tournament(1, ParticipantType.PLAYER, 4)));
            when(registerUserRepository.countByTournamentIdAndStatus(1, RegistrationStatus.VALIDATED))
                    .thenReturn(4L);

            ParticipantCountResponse response = service.count(1);

            assertThat(response.current()).isEqualTo(4L);
            assertThat(response.remaining()).isZero();
        }

        @Test
        @DisplayName("plus de validés que le maximum : les places restantes ne sont jamais négatives")
        void moreValidatedThanMax() {
            when(tournamentRepository.findById(1))
                    .thenReturn(Optional.of(tournament(1, ParticipantType.PLAYER, 4)));
            when(registerUserRepository.countByTournamentIdAndStatus(1, RegistrationStatus.VALIDATED))
                    .thenReturn(5L);

            ParticipantCountResponse response = service.count(1);

            assertThat(response.current()).isEqualTo(5L);
            assertThat(response.remaining()).isZero();
        }
    }
}
