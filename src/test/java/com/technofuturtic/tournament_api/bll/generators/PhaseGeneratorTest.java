package com.technofuturtic.tournament_api.bll.generators;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import com.technofuturtic.tournament_api.dal.repositories.PhaseRepository;
import com.technofuturtic.tournament_api.dl.entities.PhaseEntity;
import com.technofuturtic.tournament_api.dl.entities.TournamentEntity;
import com.technofuturtic.tournament_api.dl.enums.PhaseType;
import com.technofuturtic.tournament_api.dl.enums.TournamentFormat;
import org.junit.jupiter.api.BeforeEach;
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
class PhaseGeneratorTest {

    @Mock
    private PhaseRepository phaseRepository;

    @InjectMocks
    private PhaseGenerator phaseGenerator;

    @Captor
    private ArgumentCaptor<List<PhaseEntity>> phasesCaptor;

    private TournamentEntity tournament;

    @BeforeEach
    void setUp() {
        tournament = new TournamentEntity();
        ReflectionTestUtils.setField(tournament, "id", 1);
    }

    private void stubSaveAllReturnsInput() {
        when(phaseRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    @DisplayName("SINGLE_ELIMINATION : une seule phase ELIMINATION")
    void generate_singleElimination() {
        tournament.setFormat(TournamentFormat.SINGLE_ELIMINATION);
        stubSaveAllReturnsInput();

        List<PhaseEntity> phases = phaseGenerator.generate(tournament);

        assertThat(phases).hasSize(1);
        assertThat(phases.get(0).getType()).isEqualTo(PhaseType.ELIMINATION);
        assertThat(phases.get(0).getOrderIndex()).isEqualTo(1);
        assertThat(phases.get(0).getTournament()).isSameAs(tournament);
    }

    @Test
    @DisplayName("DOUBLE_ELIMINATION : ELIMINATION puis LOSERS_BRACKET")
    void generate_doubleElimination() {
        tournament.setFormat(TournamentFormat.DOUBLE_ELIMINATION);
        stubSaveAllReturnsInput();

        List<PhaseEntity> phases = phaseGenerator.generate(tournament);

        assertThat(phases).extracting(PhaseEntity::getType)
                .containsExactly(PhaseType.ELIMINATION, PhaseType.LOSERS_BRACKET);
        assertThat(phases).extracting(PhaseEntity::getOrderIndex).containsExactly(1, 2);
        assertThat(phases).allSatisfy(phase -> assertThat(phase.getTournament()).isSameAs(tournament));
    }

    @Test
    @DisplayName("GROUPS_THEN_PLAYOFF : GROUP_STAGE puis ELIMINATION")
    void generate_groupsThenPlayoff() {
        tournament.setFormat(TournamentFormat.GROUPS_THEN_PLAYOFF);
        stubSaveAllReturnsInput();

        List<PhaseEntity> phases = phaseGenerator.generate(tournament);

        assertThat(phases).extracting(PhaseEntity::getType)
                .containsExactly(PhaseType.GROUP_STAGE, PhaseType.ELIMINATION);
        assertThat(phases).extracting(PhaseEntity::getOrderIndex).containsExactly(1, 2);
        assertThat(phases).allSatisfy(phase -> assertThat(phase.getTournament()).isSameAs(tournament));
    }

    @Test
    @DisplayName("generate : persiste les phases via saveAll et retourne le résultat du repository")
    void generate_returnsSavedPhases() {
        tournament.setFormat(TournamentFormat.SINGLE_ELIMINATION);
        PhaseEntity savedPhase = new PhaseEntity();
        ReflectionTestUtils.setField(savedPhase, "id", 42);
        when(phaseRepository.saveAll(anyList())).thenReturn(List.of(savedPhase));

        List<PhaseEntity> result = phaseGenerator.generate(tournament);

        verify(phaseRepository).saveAll(phasesCaptor.capture());
        assertThat(phasesCaptor.getValue()).hasSize(1);
        assertThat(result).containsExactly(savedPhase);
    }
}