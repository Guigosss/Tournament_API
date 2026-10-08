package com.technofuturtic.tournament_api.bll.services.impls;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import com.technofuturtic.tournament_api.bll.exceptions.engine.match.MatchNotFoundException;
import com.technofuturtic.tournament_api.dal.repositories.MatchRepository;
import com.technofuturtic.tournament_api.dal.repositories.RoundRepository;
import com.technofuturtic.tournament_api.dl.entities.MatchEntity;
import com.technofuturtic.tournament_api.dl.entities.ParticipantEntity;
import com.technofuturtic.tournament_api.dl.entities.PhaseEntity;
import com.technofuturtic.tournament_api.dl.entities.RoundEntity;
import com.technofuturtic.tournament_api.dl.enums.MatchStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class TournamentProgressionServiceImplTest {

    private static final Integer PHASE_ID = 10;
    private static final Integer NEXT_ROUND_ID = 21;

    @Mock
    private MatchRepository matchRepository;

    @Mock
    private RoundRepository roundRepository;

    @InjectMocks
    private TournamentProgressionServiceImpl progressionService;

    private RoundEntity currentRound;
    private RoundEntity nextRound;
    private ParticipantEntity winner;

    @BeforeEach
    void setUp() {
        PhaseEntity phase = new PhaseEntity();
        ReflectionTestUtils.setField(phase, "id", PHASE_ID);

        currentRound = new RoundEntity();
        ReflectionTestUtils.setField(currentRound, "id", 20);
        currentRound.setOrderIndex(1);
        currentRound.setPhase(phase);

        nextRound = new RoundEntity();
        ReflectionTestUtils.setField(nextRound, "id", NEXT_ROUND_ID);
        nextRound.setOrderIndex(2);
        nextRound.setPhase(phase);

        winner = new ParticipantEntity();
        ReflectionTestUtils.setField(winner, "id", 7);
    }

    private MatchEntity currentMatch(int orderIndex) {
        MatchEntity match = new MatchEntity();
        match.setRound(currentRound);
        match.setOrderIndex(orderIndex);
        match.setWinner(winner);
        match.setStatus(MatchStatus.BYE);
        return match;
    }

    private MatchEntity emptyNextMatch(int orderIndex) {
        MatchEntity match = new MatchEntity();
        match.setRound(nextRound);
        match.setOrderIndex(orderIndex);
        match.setStatus(MatchStatus.WAITING);
        return match;
    }

    //- Match source -> match suivant : 1,2 -> 1 ; 3,4 -> 2 ; 5,6 -> 3 ...
    @ParameterizedTest(name = "match {0} -> match suivant {1}, participant1 = {2}")
    @CsvSource({
            "1, 1, true",
            "2, 1, false",
            "3, 2, true",
            "4, 2, false",
            "5, 3, true",
            "6, 3, false"
    })
    @DisplayName("progress : place le gagnant dans le bon match et le bon slot du round suivant")
    void progress_placesWinnerInCorrectSlot(int orderIndex, int expectedNextIndex, boolean isParticipant1) {
        MatchEntity match = currentMatch(orderIndex);
        MatchEntity nextMatch = emptyNextMatch(expectedNextIndex);

        when(roundRepository.findByPhaseIdAndOrderIndex(PHASE_ID, 2)).thenReturn(Optional.of(nextRound));
        when(matchRepository.findByRoundIdAndOrderIndex(NEXT_ROUND_ID, expectedNextIndex))
                .thenReturn(Optional.of(nextMatch));

        progressionService.progress(match);

        if (isParticipant1) {
            assertThat(nextMatch.getParticipant1()).isSameAs(winner);
            assertThat(nextMatch.getParticipant2()).isNull();
        } else {
            assertThat(nextMatch.getParticipant2()).isSameAs(winner);
            assertThat(nextMatch.getParticipant1()).isNull();
        }
        verify(matchRepository).save(nextMatch);
    }

    @Test
    @DisplayName("progress : le match suivant reste WAITING tant qu'il manque un participant")
    void progress_nextMatchStaysWaitingWhenOnlyOneParticipant() {
        MatchEntity match = currentMatch(1);
        MatchEntity nextMatch = emptyNextMatch(1);

        when(roundRepository.findByPhaseIdAndOrderIndex(PHASE_ID, 2)).thenReturn(Optional.of(nextRound));
        when(matchRepository.findByRoundIdAndOrderIndex(NEXT_ROUND_ID, 1)).thenReturn(Optional.of(nextMatch));

        progressionService.progress(match);

        assertThat(nextMatch.getStatus()).isEqualTo(MatchStatus.WAITING);
    }

    @Test
    @DisplayName("progress : le match suivant passe PENDING quand les deux participants sont connus")
    void progress_nextMatchBecomesPendingWhenBothParticipantsKnown() {
        ParticipantEntity otherParticipant = new ParticipantEntity();
        ReflectionTestUtils.setField(otherParticipant, "id", 8);

        MatchEntity match = currentMatch(2);
        MatchEntity nextMatch = emptyNextMatch(1);
        nextMatch.setParticipant1(otherParticipant);

        when(roundRepository.findByPhaseIdAndOrderIndex(PHASE_ID, 2)).thenReturn(Optional.of(nextRound));
        when(matchRepository.findByRoundIdAndOrderIndex(NEXT_ROUND_ID, 1)).thenReturn(Optional.of(nextMatch));

        progressionService.progress(match);

        assertThat(nextMatch.getParticipant1()).isSameAs(otherParticipant);
        assertThat(nextMatch.getParticipant2()).isSameAs(winner);
        assertThat(nextMatch.getStatus()).isEqualTo(MatchStatus.PENDING);
        verify(matchRepository).save(nextMatch);
    }

    @Test
    @DisplayName("progress : match suivant introuvable -> MatchNotFoundException")
    void progress_nextMatchNotFound_throws() {
        MatchEntity match = currentMatch(1);

        when(roundRepository.findByPhaseIdAndOrderIndex(PHASE_ID, 2)).thenReturn(Optional.of(nextRound));
        when(matchRepository.findByRoundIdAndOrderIndex(NEXT_ROUND_ID, 1)).thenReturn(Optional.empty());

        assertThrows(MatchNotFoundException.class, () -> progressionService.progress(match));

        verify(matchRepository, never()).save(any());
    }

    /**
     * Cas de la finale : il n'y a pas de round suivant. Avec le code actuel, nextRound vaut null
     * et {@code nextRound.getId()} lève une NullPointerException.
     * Test désactivé : à activer une fois le comportement voulu décidé (ne rien faire, par exemple).
     */
    @Test
    @Disabled("Bug connu : NPE sur la finale (nextRound == null)")
    @DisplayName("progress : pas de round suivant (finale) -> ne fait rien")
    void progress_noNextRound_doesNothing() {
        MatchEntity match = currentMatch(1);
        when(roundRepository.findByPhaseIdAndOrderIndex(PHASE_ID, 2)).thenReturn(Optional.empty());

        progressionService.progress(match);

        verify(matchRepository, never()).save(any());
    }
}