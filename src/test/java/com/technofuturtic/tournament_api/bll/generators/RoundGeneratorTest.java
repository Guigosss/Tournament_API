package com.technofuturtic.tournament_api.bll.generators;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import com.technofuturtic.tournament_api.bll.exceptions.engine.round.EliminationParticipantCountOutOfRangeException;
import com.technofuturtic.tournament_api.bll.generators.rules.EliminationRules;
import com.technofuturtic.tournament_api.dal.repositories.RoundRepository;
import com.technofuturtic.tournament_api.dl.entities.PhaseEntity;
import com.technofuturtic.tournament_api.dl.entities.RoundEntity;
import com.technofuturtic.tournament_api.dl.enums.PhaseType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class RoundGeneratorTest {

    @Mock
    private RoundRepository roundRepository;

    @InjectMocks
    private RoundGenerator roundGenerator;

    private PhaseEntity eliminationPhase;

    @BeforeEach
    void setUp() {
        eliminationPhase = new PhaseEntity();
        ReflectionTestUtils.setField(eliminationPhase, "id", 1);
        eliminationPhase.setType(PhaseType.ELIMINATION);
    }

    private void stubSaveAllReturnsInput() {
        when(roundRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));
    }

    //- ELIMINATION
    //- participants -> nombre de rounds attendu (hypothèse : RoundType va de FINAL à ROUND_OF_64)
    @ParameterizedTest(name = "{0} participants -> {1} rounds")
    @CsvSource({
            "2, 1",
            "3, 2",
            "4, 2",
            "5, 3",
            "8, 3",
            "9, 4",
            "16, 4"
    })
    @DisplayName("ELIMINATION : le nombre de rounds dépend de la puissance de 2 supérieure")
    void generate_elimination_roundCount(int participantCount, int expectedRounds) {
        stubSaveAllReturnsInput();

        List<RoundEntity> rounds = roundGenerator.generate(eliminationPhase, participantCount);

        assertThat(rounds).hasSize(expectedRounds);
    }

    @Test
    @DisplayName("ELIMINATION : orderIndex de 1 à n, rattachés à la phase")
    void generate_elimination_setsOrderIndexAndPhase() {
        stubSaveAllReturnsInput();

        List<RoundEntity> rounds = roundGenerator.generate(eliminationPhase, 8);

        assertThat(rounds).extracting(RoundEntity::getOrderIndex).containsExactly(1, 2, 3);
        assertThat(rounds).allSatisfy(round -> assertThat(round.getPhase()).isSameAs(eliminationPhase));
    }

    @Test
    @DisplayName("ELIMINATION : le premier round a la taille de bracket attendue et la finale est la dernière")
    void generate_elimination_firstRoundHasBracketSize() {
        stubSaveAllReturnsInput();

        List<RoundEntity> rounds = roundGenerator.generate(eliminationPhase, 8);

        assertThat(rounds.get(0).getType().getBracketSize()).isEqualTo(8);
        assertThat(rounds.get(rounds.size() - 1).getType().getBracketSize()).isEqualTo(2);
    }

    @Test
    @DisplayName("ELIMINATION : 5 participants -> bracket de 8 (premier round de 8)")
    void generate_elimination_nonPowerOfTwo_usesNextPowerOfTwo() {
        stubSaveAllReturnsInput();

        List<RoundEntity> rounds = roundGenerator.generate(eliminationPhase, 5);

        assertThat(rounds.get(0).getType().getBracketSize()).isEqualTo(8);
    }

    @Test
    @DisplayName("ELIMINATION : persiste les rounds et retourne ce que renvoie le repository")
    void generate_elimination_returnsSavedRounds() {
        RoundEntity saved = new RoundEntity();
        ReflectionTestUtils.setField(saved, "id", 99);
        when(roundRepository.saveAll(anyList())).thenReturn(List.of(saved));

        List<RoundEntity> result = roundGenerator.generate(eliminationPhase, 2);

        verify(roundRepository).saveAll(anyList());
        assertThat(result).containsExactly(saved);
    }

    //- Types de phase non implémentés
    @ParameterizedTest
    @EnumSource(value = PhaseType.class, names = {"GROUP_STAGE", "LOSERS_BRACKET"})
    @DisplayName("GROUP_STAGE / LOSERS_BRACKET : liste vide pour l'instant (TODO)")
    void generate_notImplementedTypes_returnEmptyList(PhaseType type) {
        PhaseEntity phase = new PhaseEntity();
        phase.setType(type);
        when(roundRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

        List<RoundEntity> rounds = roundGenerator.generate(phase, 8);

        assertThat(rounds).isEmpty();
    }

    //- Validation
    @Test
    @DisplayName("participants < MIN_PARTICIPANTS -> EliminationParticipantCountOutOfRangeException")
    void generate_belowMin_throws() {
        assertThrows(EliminationParticipantCountOutOfRangeException.class,
                () -> roundGenerator.generate(eliminationPhase, EliminationRules.MIN_PARTICIPANTS - 1));

        verify(roundRepository, never()).saveAll(anyList());
    }

    @Test
    @DisplayName("participants > MAX_PARTICIPANTS -> EliminationParticipantCountOutOfRangeException")
    void generate_aboveMax_throws() {
        assertThrows(EliminationParticipantCountOutOfRangeException.class,
                () -> roundGenerator.generate(eliminationPhase, EliminationRules.MAX_PARTICIPANTS + 1));

        verify(roundRepository, never()).saveAll(anyList());
    }

    @Test
    @DisplayName("bornes MIN et MAX acceptées")
    void generate_bounds_areAccepted() {
        stubSaveAllReturnsInput();

        assertThat(roundGenerator.generate(eliminationPhase, EliminationRules.MIN_PARTICIPANTS)).isNotEmpty();
        assertThat(roundGenerator.generate(eliminationPhase, EliminationRules.MAX_PARTICIPANTS)).isNotEmpty();
    }
}