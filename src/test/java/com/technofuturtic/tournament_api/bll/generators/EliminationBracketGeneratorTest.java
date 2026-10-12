package com.technofuturtic.tournament_api.bll.generators;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import com.technofuturtic.tournament_api.bll.exceptions.engine.match.TooManyParticipantsForBracketException;
import com.technofuturtic.tournament_api.bll.services.TournamentProgressionService;
import com.technofuturtic.tournament_api.dal.repositories.MatchRepository;
import com.technofuturtic.tournament_api.dl.entities.MatchEntity;
import com.technofuturtic.tournament_api.dl.entities.ParticipantEntity;
import com.technofuturtic.tournament_api.dl.entities.RoundEntity;
import com.technofuturtic.tournament_api.dl.enums.MatchStatus;
import com.technofuturtic.tournament_api.dl.enums.PhaseType;
import com.technofuturtic.tournament_api.dl.enums.RoundType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class EliminationBracketGeneratorTest {

    @Mock
    private MatchRepository matchRepository;

    @Mock
    private TournamentProgressionService tournamentProgressionService;

    @InjectMocks
    private EliminationBracketGenerator generator;

    @Captor
    private ArgumentCaptor<List<MatchEntity>> matchesCaptor;

    //- Helpers
    private static RoundType roundTypeOfSize(int bracketSize) {
        return Arrays.stream(RoundType.values())
                .filter(type -> type.getBracketSize() == bracketSize)
                .findFirst()
                .orElseThrow();
    }

    private static RoundEntity round(int id, int orderIndex, int bracketSize) {
        RoundEntity round = new RoundEntity();
        ReflectionTestUtils.setField(round, "id", id);
        round.setOrderIndex(orderIndex);
        round.setType(roundTypeOfSize(bracketSize));
        return round;
    }

    private static List<ParticipantEntity> participants(int count) {
        List<ParticipantEntity> list = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            ParticipantEntity participant = new ParticipantEntity();
            ReflectionTestUtils.setField(participant, "id", i);
            list.add(participant);
        }
        return list;
    }

    private List<MatchEntity> captureSavedMatches() {
        verify(matchRepository).saveAll(matchesCaptor.capture());
        return matchesCaptor.getValue();
    }

    //- supportedType
    @Test
    @DisplayName("supportedType retourne ELIMINATION")
    void supportedType_returnsElimination() {
        assertThat(generator.supportedType()).isEqualTo(PhaseType.ELIMINATION);
    }

    //- generateFirstRounds
    @Test
    @DisplayName("generateFirstRounds : bracket plein (8/8) -> 4 matchs PENDING sans bye")
    void generateFirstRounds_fullBracket_createsPendingMatchesOnly() {
        RoundEntity firstRound = round(1, 1, 8);
        List<ParticipantEntity> participants = participants(8);

        generator.generateFirstRounds(firstRound, participants);

        List<MatchEntity> matches = captureSavedMatches();
        assertThat(matches).hasSize(4);
        assertThat(matches).allSatisfy(match -> {
            assertThat(match.getStatus()).isEqualTo(MatchStatus.PENDING);
            assertThat(match.getParticipant1()).isNotNull();
            assertThat(match.getParticipant2()).isNotNull();
            assertThat(match.getWinner()).isNull();
            assertThat(match.getRound()).isSameAs(firstRound);
            assertThat(match.getScore1()).isZero();
            assertThat(match.getScore2()).isZero();
        });
    }

    @Test
    @DisplayName("generateFirstRounds : tous les participants sont placés exactement une fois")
    void generateFirstRounds_placesEachParticipantExactlyOnce() {
        RoundEntity firstRound = round(1, 1, 8);
        List<ParticipantEntity> participants = participants(6);

        generator.generateFirstRounds(firstRound, participants);

        List<ParticipantEntity> placed = captureSavedMatches().stream()
                .flatMap(match -> java.util.stream.Stream.of(match.getParticipant1(), match.getParticipant2()))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        assertThat(placed).hasSize(6);
        assertThat(new HashSet<>(placed)).containsExactlyInAnyOrderElementsOf(participants);
    }

    @Test
    @DisplayName("generateFirstRounds : 5 participants sur bracket de 8 -> 3 byes et 1 match PENDING")
    void generateFirstRounds_withByes_createsByeMatchesWithWinner() {
        RoundEntity firstRound = round(1, 1, 8);
        List<ParticipantEntity> participants = participants(5);

        generator.generateFirstRounds(firstRound, participants);

        List<MatchEntity> matches = captureSavedMatches();
        List<MatchEntity> byeMatches = matches.stream()
                .filter(match -> match.getStatus() == MatchStatus.BYE)
                .toList();
        List<MatchEntity> pendingMatches = matches.stream()
                .filter(match -> match.getStatus() == MatchStatus.PENDING)
                .toList();

        assertThat(matches).hasSize(4);
        assertThat(byeMatches).hasSize(3);
        assertThat(pendingMatches).hasSize(1);
        assertThat(byeMatches).allSatisfy(match -> {
            assertThat(match.getParticipant1()).isNotNull();
            assertThat(match.getParticipant2()).isNull();
            assertThat(match.getWinner()).isSameAs(match.getParticipant1());
        });
    }

    @Test
    @DisplayName("generateFirstRounds : jamais deux byes dans le même match")
    void generateFirstRounds_neverCreatesDoubleBye() {
        RoundEntity firstRound = round(1, 1, 16);
        //- 9 participants -> 7 byes sur 8 matchs : cas le plus tendu
        for (int run = 0; run < 50; run++) {
            org.mockito.Mockito.clearInvocations(matchRepository);
            generator.generateFirstRounds(firstRound, participants(9));

            assertThat(captureSavedMatches()).noneMatch(match ->
                    match.getParticipant1() == null && match.getParticipant2() == null);
        }
    }

    @Test
    @DisplayName("generateFirstRounds : les matchs sont numérotés de 1 à n")
    void generateFirstRounds_setsOrderIndexFromOne() {
        RoundEntity firstRound = round(1, 1, 8);

        generator.generateFirstRounds(firstRound, participants(8));

        assertThat(captureSavedMatches())
                .extracting(MatchEntity::getOrderIndex)
                .containsExactly(1, 2, 3, 4);
    }

    @Test
    @DisplayName("generateFirstRounds : trop de participants -> TooManyParticipantsForBracketException")
    void generateFirstRounds_tooManyParticipants_throws() {
        RoundEntity firstRound = round(1, 1, 4);

        assertThrows(TooManyParticipantsForBracketException.class,
                () -> generator.generateFirstRounds(firstRound, participants(5)));

        verify(matchRepository, times(0)).saveAll(anyList());
    }

    @Test
    @DisplayName("generateFirstRounds : ne modifie pas la liste de participants d'origine")
    void generateFirstRounds_doesNotMutateInputList() {
        List<ParticipantEntity> participants = participants(8);
        List<ParticipantEntity> copy = new ArrayList<>(participants);

        generator.generateFirstRounds(round(1, 1, 8), participants);

        assertThat(participants).containsExactlyElementsOf(copy);
    }

    //- generateEmpty
    @Test
    @DisplayName("generateEmpty : crée bracketSize/2 matchs WAITING sans participant")
    void generateEmpty_createsWaitingMatches() {
        RoundEntity round = round(2, 2, 4);

        generator.generateEmpty(round);

        List<MatchEntity> matches = captureSavedMatches();
        assertThat(matches).hasSize(2);
        assertThat(matches).allSatisfy(match -> {
            assertThat(match.getStatus()).isEqualTo(MatchStatus.WAITING);
            assertThat(match.getParticipant1()).isNull();
            assertThat(match.getParticipant2()).isNull();
            assertThat(match.getWinner()).isNull();
            assertThat(match.getRound()).isSameAs(round);
        });
        assertThat(matches).extracting(MatchEntity::getOrderIndex).containsExactly(1, 2);
    }

    //- generate
    @Test
    @DisplayName("generate : premier round rempli, rounds suivants vides, byes propagés")
    void generate_buildsAllRoundsAndProgressesByes() {
        RoundEntity firstRound = round(1, 1, 8);
        RoundEntity semiFinal = round(2, 2, 4);
        RoundEntity finalRound = round(3, 3, 2);
        List<RoundEntity> rounds = List.of(firstRound, semiFinal, finalRound);

        MatchEntity byeMatch1 = new MatchEntity();
        byeMatch1.setOrderIndex(1);
        MatchEntity byeMatch2 = new MatchEntity();
        byeMatch2.setOrderIndex(2);
        when(matchRepository.findByRoundIdAndStatus(1, MatchStatus.BYE))
                .thenReturn(List.of(byeMatch1, byeMatch2));

        generator.generate(rounds, participants(6));

        //- 3 rounds -> 3 saveAll
        verify(matchRepository, times(3)).saveAll(matchesCaptor.capture());
        List<List<MatchEntity>> allSaves = matchesCaptor.getAllValues();
        assertThat(allSaves.get(0)).hasSize(4);
        assertThat(allSaves.get(1)).hasSize(2).allMatch(m -> m.getStatus() == MatchStatus.WAITING);
        assertThat(allSaves.get(2)).hasSize(1).allMatch(m -> m.getStatus() == MatchStatus.WAITING);

        verify(tournamentProgressionService).progress(byeMatch1);
        verify(tournamentProgressionService).progress(byeMatch2);
    }

    @Test
    @DisplayName("generate : aucun bye -> aucune progression")
    void generate_noBye_doesNotProgress() {
        RoundEntity firstRound = round(1, 1, 4);
        RoundEntity finalRound = round(2, 2, 2);
        when(matchRepository.findByRoundIdAndStatus(1, MatchStatus.BYE)).thenReturn(List.of());

        generator.generate(List.of(firstRound, finalRound), participants(4));

        org.mockito.Mockito.verifyNoInteractions(tournamentProgressionService);
    }

    @Test
    @DisplayName("generate : les participants ne sont placés que dans le premier round")
    void generate_participantsOnlyInFirstRound() {
        RoundEntity firstRound = round(1, 1, 4);
        RoundEntity finalRound = round(2, 2, 2);
        when(matchRepository.findByRoundIdAndStatus(1, MatchStatus.BYE)).thenReturn(List.of());

        generator.generate(List.of(firstRound, finalRound), participants(4));

        verify(matchRepository, times(2)).saveAll(matchesCaptor.capture());
        Set<ParticipantEntity> inSecondRound = matchesCaptor.getAllValues().get(1).stream()
                .flatMap(match -> java.util.stream.Stream.of(match.getParticipant1(), match.getParticipant2()))
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        assertThat(inSecondRound).isEmpty();
    }
}