package com.technofuturtic.tournament_api.bll.services.impls;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import com.technofuturtic.tournament_api.api.models.tournament.responses.MatchResponse;
import com.technofuturtic.tournament_api.api.models.tournament.responses.RoundResponse;
import com.technofuturtic.tournament_api.api.models.tournament.responses.PhaseResponse;
import com.technofuturtic.tournament_api.api.models.tournament.responses.PhaseBracketResponse;
import com.technofuturtic.tournament_api.api.models.tournament.responses.RoundBracketResponse;
import com.technofuturtic.tournament_api.api.models.tournament.responses.TournamentBracketResponse;
import com.technofuturtic.tournament_api.bll.exceptions.engine.tournament.TournamentNotFoundException;
import com.technofuturtic.tournament_api.dal.repositories.MatchRepository;
import com.technofuturtic.tournament_api.dal.repositories.PhaseRepository;
import com.technofuturtic.tournament_api.dal.repositories.RoundRepository;
import com.technofuturtic.tournament_api.dal.repositories.TournamentRepository;
import com.technofuturtic.tournament_api.dl.entities.PhaseEntity;
import com.technofuturtic.tournament_api.dl.entities.RoundEntity;
import com.technofuturtic.tournament_api.dl.entities.MatchEntity;
import com.technofuturtic.tournament_api.dl.entities.ParticipantEntity;
import com.technofuturtic.tournament_api.dl.entities.TournamentEntity;
import com.technofuturtic.tournament_api.dl.enums.MatchStatus;
import com.technofuturtic.tournament_api.dl.enums.PhaseType;
import com.technofuturtic.tournament_api.dl.enums.RoundType;
import com.technofuturtic.tournament_api.dl.enums.TournamentStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class TournamentGenerationServiceImplTest {

    private static final Integer TOURNAMENT_ID = 1;

    @Mock
    private TournamentRepository tournamentRepository;

    @Mock
    private PhaseRepository phaseRepository;

    @Mock
    private RoundRepository roundRepository;

    @Mock
    private MatchRepository matchRepository;

    @InjectMocks
    private TournamentGenerationServiceImpl service;

    //- Helpers
    private static TournamentEntity tournament() {
        TournamentEntity tournament = new TournamentEntity();
        ReflectionTestUtils.setField(tournament, "id", TOURNAMENT_ID);
        tournament.setStatus(TournamentStatus.IN_PROGRESS);
        return tournament;
    }

    private static PhaseEntity phase(int id, int orderIndex, PhaseType type, TournamentEntity tournament) {
        PhaseEntity phase = new PhaseEntity();
        ReflectionTestUtils.setField(phase, "id", id);
        phase.setOrderIndex(orderIndex);
        phase.setType(type);
        phase.setTournament(tournament);
        return phase;
    }

    private static RoundEntity round(int id, int orderIndex, RoundType type, PhaseEntity phase) {
        RoundEntity round = new RoundEntity();
        ReflectionTestUtils.setField(round, "id", id);
        round.setOrderIndex(orderIndex);
        round.setType(type);
        round.setPhase(phase);
        return round;
    }

    private static ParticipantEntity participant(int id) {
        ParticipantEntity participant = new ParticipantEntity();
        ReflectionTestUtils.setField(participant, "id", id);
        return participant;
    }

    private static MatchEntity match(int id, int orderIndex, RoundEntity round) {
        MatchEntity match = new MatchEntity();
        ReflectionTestUtils.setField(match, "id", id);
        match.setOrderIndex(orderIndex);
        match.setRound(round);
        match.setStatus(MatchStatus.WAITING);
        match.setScore1(0);
        match.setScore2(0);
        return match;
    }

    //- getBracket
    @Test
    @DisplayName("getBracket : tournoi introuvable -> TournamentNotFoundException")
    void getBracket_tournamentNotFound_throws() {
        when(tournamentRepository.findById(TOURNAMENT_ID)).thenReturn(Optional.empty());

        assertThrows(TournamentNotFoundException.class, () -> service.getBracket(TOURNAMENT_ID));

        verifyNoInteractions(phaseRepository, roundRepository, matchRepository);
    }

    @Test
    @DisplayName("getBracket : reconstruit toute la hiérarchie phases > rounds > matchs")
    void getBracket_buildsFullHierarchy() {
        TournamentEntity tournament = tournament();
        PhaseEntity phase1 = phase(10, 1, PhaseType.GROUP_STAGE, tournament);
        PhaseEntity phase2 = phase(11, 2, PhaseType.ELIMINATION, tournament);
        RoundType[] roundTypes = RoundType.values();
        RoundEntity round1 = round(20, 1, roundTypes[0], phase1);
        RoundEntity round2 = round(21, 1, roundTypes[0], phase2);
        RoundEntity round3 = round(22, 2, roundTypes[roundTypes.length - 1], phase2);

        when(tournamentRepository.findById(TOURNAMENT_ID)).thenReturn(Optional.of(tournament));
        when(phaseRepository.findByTournamentIdOrderByOrderIndexAsc(TOURNAMENT_ID))
                .thenReturn(List.of(phase1, phase2));
        when(roundRepository.findByPhaseIdOrderByOrderIndex(10)).thenReturn(List.of(round1));
        when(roundRepository.findByPhaseIdOrderByOrderIndex(11)).thenReturn(List.of(round2, round3));
        when(matchRepository.findByRoundId(20)).thenReturn(List.of(match(30, 1, round1)));
        when(matchRepository.findByRoundId(21)).thenReturn(List.of(match(31, 1, round2), match(32, 2, round2)));
        when(matchRepository.findByRoundId(22)).thenReturn(List.of(match(33, 1, round3)));

        TournamentBracketResponse response = service.getBracket(TOURNAMENT_ID);

        assertThat(response.tournamentId()).isEqualTo(TOURNAMENT_ID);
        assertThat(response.phases()).hasSize(2);

        PhaseBracketResponse groupPhase = response.phases().get(0);
        assertThat(groupPhase.id()).isEqualTo(10);
        assertThat(groupPhase.type()).isEqualTo(PhaseType.GROUP_STAGE);
        assertThat(groupPhase.orderIndex()).isEqualTo(1);
        assertThat(groupPhase.rounds()).hasSize(1);
        assertThat(groupPhase.rounds().get(0).matches()).extracting(MatchResponse::id).containsExactly(30);

        PhaseBracketResponse eliminationPhase = response.phases().get(1);
        assertThat(eliminationPhase.id()).isEqualTo(11);
        assertThat(eliminationPhase.type()).isEqualTo(PhaseType.ELIMINATION);
        assertThat(eliminationPhase.rounds()).extracting(RoundBracketResponse::id).containsExactly(21, 22);
        assertThat(eliminationPhase.rounds()).extracting(RoundBracketResponse::type)
                .containsExactly(roundTypes[0], roundTypes[roundTypes.length - 1]);
        assertThat(eliminationPhase.rounds().get(0).matches()).extracting(MatchResponse::id)
                .containsExactly(31, 32);
        assertThat(eliminationPhase.rounds().get(1).matches()).extracting(MatchResponse::id)
                .containsExactly(33);
    }

    @Test
    @DisplayName("getBracket : tournoi sans phase -> bracket vide, aucune requête sur rounds/matchs")
    void getBracket_noPhases_returnsEmptyBracket() {
        when(tournamentRepository.findById(TOURNAMENT_ID)).thenReturn(Optional.of(tournament()));
        when(phaseRepository.findByTournamentIdOrderByOrderIndexAsc(TOURNAMENT_ID)).thenReturn(List.of());

        TournamentBracketResponse response = service.getBracket(TOURNAMENT_ID);

        assertThat(response.tournamentId()).isEqualTo(TOURNAMENT_ID);
        assertThat(response.phases()).isEmpty();
        verifyNoInteractions(roundRepository, matchRepository);
    }

    //- getPhases
    @Test
    @DisplayName("getPhases : mappe chaque phase en PhaseResponse en gardant l'ordre")
    void getPhases_mapsEntities() {
        TournamentEntity tournament = tournament();
        when(phaseRepository.findByTournamentIdOrderByOrderIndexAsc(TOURNAMENT_ID))
                .thenReturn(List.of(
                        phase(10, 1, PhaseType.GROUP_STAGE, tournament),
                        phase(11, 2, PhaseType.ELIMINATION, tournament)));

        List<PhaseResponse> responses = service.getPhases(TOURNAMENT_ID);

        assertThat(responses).containsExactly(
                new PhaseResponse(10, PhaseType.GROUP_STAGE, 1),
                new PhaseResponse(11, PhaseType.ELIMINATION, 2));
    }

    @Test
    @DisplayName("getPhases : aucune phase -> liste vide")
    void getPhases_empty() {
        when(phaseRepository.findByTournamentIdOrderByOrderIndexAsc(TOURNAMENT_ID)).thenReturn(List.of());

        assertThat(service.getPhases(TOURNAMENT_ID)).isEmpty();
    }

    //- getRounds
    @Test
    @DisplayName("getRounds : mappe les rounds du tournoi et de la phase demandés")
    void getRounds_mapsEntities() {
        PhaseEntity phase = phase(10, 1, PhaseType.ELIMINATION, tournament());
        RoundType[] roundTypes = RoundType.values();
        when(roundRepository.findByTournamentAndPhase(TOURNAMENT_ID, 10))
                .thenReturn(List.of(
                        round(20, 1, roundTypes[0], phase),
                        round(21, 2, roundTypes[roundTypes.length - 1], phase)));

        List<RoundResponse> responses = service.getRounds(TOURNAMENT_ID, 10);

        assertThat(responses).containsExactly(
                new RoundResponse(20, roundTypes[0], 1),
                new RoundResponse(21, roundTypes[roundTypes.length - 1], 2));
        verify(roundRepository).findByTournamentAndPhase(TOURNAMENT_ID, 10);
    }

    @Test
    @DisplayName("getRounds : aucun round -> liste vide")
    void getRounds_empty() {
        when(roundRepository.findByTournamentAndPhase(TOURNAMENT_ID, 10)).thenReturn(List.of());

        assertThat(service.getRounds(TOURNAMENT_ID, 10)).isEmpty();
    }

    //- getMatches
    @Test
    @DisplayName("getMatches : mappe participants, scores, gagnant et statut")
    void getMatches_mapsEntities() {
        PhaseEntity phase = phase(10, 1, PhaseType.ELIMINATION, tournament());
        RoundEntity round = round(20, 1, RoundType.values()[0], phase);

        MatchEntity finishedMatch = match(30, 1, round);
        ParticipantEntity participant1 = participant(5);
        ParticipantEntity participant2 = participant(6);
        finishedMatch.setParticipant1(participant1);
        finishedMatch.setParticipant2(participant2);
        finishedMatch.setScore1(3);
        finishedMatch.setScore2(1);
        finishedMatch.setWinner(participant1);
        finishedMatch.setStatus(MatchStatus.PENDING);

        MatchEntity emptyMatch = match(31, 2, round);

        when(matchRepository.findByTournamentPhaseAndRound(TOURNAMENT_ID, 10, 20))
                .thenReturn(List.of(finishedMatch, emptyMatch));

        List<MatchResponse> responses = service.getMatches(TOURNAMENT_ID, 10, 20);

        assertThat(responses).hasSize(2);

        MatchResponse first = responses.get(0);
        assertThat(first.id()).isEqualTo(30);
        assertThat(first.participant1().id()).isEqualTo(5);
        assertThat(first.participant2().id()).isEqualTo(6);
        assertThat(first.score1()).isEqualTo(3);
        assertThat(first.score2()).isEqualTo(1);
        assertThat(first.winner().id()).isEqualTo(5);
        assertThat(first.status()).isEqualTo(MatchStatus.PENDING);
        assertThat(first.orderIndex()).isEqualTo(1);

        MatchResponse second = responses.get(1);
        assertThat(second.id()).isEqualTo(31);
        assertThat(second.participant1()).isNull();
        assertThat(second.participant2()).isNull();
        assertThat(second.winner()).isNull();
        assertThat(second.status()).isEqualTo(MatchStatus.WAITING);
        assertThat(second.orderIndex()).isEqualTo(2);
    }

    @Test
    @DisplayName("getMatches : aucun match -> liste vide, sans passer par le TournamentRepository")
    void getMatches_empty() {
        when(matchRepository.findByTournamentPhaseAndRound(TOURNAMENT_ID, 10, 20)).thenReturn(List.of());

        assertThat(service.getMatches(TOURNAMENT_ID, 10, 20)).isEmpty();
        verify(tournamentRepository, never()).findById(any());
    }
}