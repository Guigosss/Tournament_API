package com.technofuturtic.tournament_api.bll.generators;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.technofuturtic.tournament_api.bll.exceptions.engine.tournament.TournamentMustHaveParticipantsException;
import com.technofuturtic.tournament_api.bll.exceptions.engine.tournament.TournamentNotFoundException;
import com.technofuturtic.tournament_api.bll.exceptions.engine.tournament.TournamentNotReadyToStartException;
import com.technofuturtic.tournament_api.bll.exceptions.engine.tournament.TournamentParticipantLimitExceededException;
import com.technofuturtic.tournament_api.dal.repositories.ParticipantRepository;
import com.technofuturtic.tournament_api.dal.repositories.TournamentRepository;
import com.technofuturtic.tournament_api.dl.entities.ParticipantEntity;
import com.technofuturtic.tournament_api.dl.entities.PhaseEntity;
import com.technofuturtic.tournament_api.dl.entities.RoundEntity;
import com.technofuturtic.tournament_api.dl.entities.TournamentEntity;
import com.technofuturtic.tournament_api.dl.enums.TournamentStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class TournamentGeneratorTest {

    private static final Integer TOURNAMENT_ID = 1;

    @Mock
    private TournamentRepository tournamentRepository;

    @Mock
    private ParticipantRepository participantRepository;

    @Mock
    private PhaseGenerator phaseGenerator;

    @Mock
    private RoundGenerator roundGenerator;

    @Mock
    private EliminationBracketGenerator phaseMatchGenerator;

    @InjectMocks
    private TournamentGenerator tournamentGenerator;

    //- Helpers
    private static TournamentEntity tournament(TournamentStatus status, int maxParticipants) {
        TournamentEntity tournament = new TournamentEntity();
        ReflectionTestUtils.setField(tournament, "id", TOURNAMENT_ID);
        tournament.setStatus(status);
        tournament.setMaxParticipants(maxParticipants);
        return tournament;
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

    //- Cas nominal
    @Test
    @DisplayName("generate : orchestre phases, rounds, matchs puis passe le tournoi IN_PROGRESS")
    void generate_happyPath() {
        TournamentEntity tournament = tournament(TournamentStatus.REGISTRATION_CLOSED, 8);
        List<ParticipantEntity> participants = participants(4);
        PhaseEntity phase1 = new PhaseEntity();
        phase1.setOrderIndex(1);
        PhaseEntity phase2 = new PhaseEntity();
        phase2.setOrderIndex(2);
        RoundEntity round1 = new RoundEntity();
        round1.setOrderIndex(1);
        RoundEntity round2 = new RoundEntity();
        round2.setOrderIndex(2);
        RoundEntity round3 = new RoundEntity();
        round3.setOrderIndex(3);

        when(tournamentRepository.findById(TOURNAMENT_ID)).thenReturn(Optional.of(tournament));
        when(participantRepository.findByTournamentId(TOURNAMENT_ID)).thenReturn(participants);
        when(phaseGenerator.generate(tournament)).thenReturn(List.of(phase1, phase2));
        when(roundGenerator.generate(phase1, 4)).thenReturn(List.of(round1, round2));
        when(roundGenerator.generate(phase2, 4)).thenReturn(List.of(round3));

        tournamentGenerator.generate(TOURNAMENT_ID);

        InOrder inOrder = inOrder(phaseGenerator, roundGenerator, phaseMatchGenerator, tournamentRepository);
        inOrder.verify(phaseGenerator).generate(tournament);
        inOrder.verify(roundGenerator).generate(phase1, 4);
        inOrder.verify(roundGenerator).generate(phase2, 4);
        inOrder.verify(phaseMatchGenerator).generate(List.of(round1, round2, round3), participants);
        inOrder.verify(tournamentRepository).save(tournament);

        assertThat(tournament.getStatus()).isEqualTo(TournamentStatus.IN_PROGRESS);
    }

    @Test
    @DisplayName("generate : accepte exactement maxParticipants participants")
    void generate_participantsEqualToMax_isAccepted() {
        TournamentEntity tournament = tournament(TournamentStatus.REGISTRATION_CLOSED, 4);
        List<ParticipantEntity> participants = participants(4);

        when(tournamentRepository.findById(TOURNAMENT_ID)).thenReturn(Optional.of(tournament));
        when(participantRepository.findByTournamentId(TOURNAMENT_ID)).thenReturn(participants);
        when(phaseGenerator.generate(tournament)).thenReturn(List.of());

        tournamentGenerator.generate(TOURNAMENT_ID);

        verify(tournamentRepository).save(tournament);
        assertThat(tournament.getStatus()).isEqualTo(TournamentStatus.IN_PROGRESS);
    }

    //- Cas d'erreur
    @Test
    @DisplayName("generate : tournoi introuvable -> TournamentNotFoundException")
    void generate_tournamentNotFound_throws() {
        when(tournamentRepository.findById(TOURNAMENT_ID)).thenReturn(Optional.empty());

        assertThrows(TournamentNotFoundException.class, () -> tournamentGenerator.generate(TOURNAMENT_ID));

        verifyNoInteractions(phaseGenerator, roundGenerator, phaseMatchGenerator);
        verify(tournamentRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    @DisplayName("generate : liste de participants vide -> TournamentMustHaveParticipantsException")
    void generate_noParticipants_throws() {
        TournamentEntity tournament = tournament(TournamentStatus.REGISTRATION_CLOSED, 8);
        when(tournamentRepository.findById(TOURNAMENT_ID)).thenReturn(Optional.of(tournament));
        when(participantRepository.findByTournamentId(TOURNAMENT_ID)).thenReturn(List.of());

        assertThrows(TournamentMustHaveParticipantsException.class,
                () -> tournamentGenerator.generate(TOURNAMENT_ID));

        verifyNoInteractions(phaseGenerator, roundGenerator, phaseMatchGenerator);
        assertThat(tournament.getStatus()).isEqualTo(TournamentStatus.REGISTRATION_CLOSED);
    }

    @Test
    @DisplayName("generate : liste de participants null -> TournamentMustHaveParticipantsException")
    void generate_nullParticipants_throws() {
        TournamentEntity tournament = tournament(TournamentStatus.REGISTRATION_CLOSED, 8);
        when(tournamentRepository.findById(TOURNAMENT_ID)).thenReturn(Optional.of(tournament));
        when(participantRepository.findByTournamentId(TOURNAMENT_ID)).thenReturn(null);

        assertThrows(TournamentMustHaveParticipantsException.class,
                () -> tournamentGenerator.generate(TOURNAMENT_ID));

        verifyNoInteractions(phaseGenerator, roundGenerator, phaseMatchGenerator);
    }

    @Test
    @DisplayName("generate : plus de participants que le maximum -> TournamentParticipantLimitExceededException")
    void generate_tooManyParticipants_throws() {
        TournamentEntity tournament = tournament(TournamentStatus.REGISTRATION_CLOSED, 4);
        when(tournamentRepository.findById(TOURNAMENT_ID)).thenReturn(Optional.of(tournament));
        when(participantRepository.findByTournamentId(TOURNAMENT_ID)).thenReturn(participants(5));

        assertThrows(TournamentParticipantLimitExceededException.class,
                () -> tournamentGenerator.generate(TOURNAMENT_ID));

        verifyNoInteractions(phaseGenerator, roundGenerator, phaseMatchGenerator);
    }

    @Test
    @DisplayName("generate : statut différent de REGISTRATION_CLOSED -> TournamentNotReadyToStartException")
    void generate_wrongStatus_throws() {
        TournamentEntity tournament = tournament(TournamentStatus.IN_PROGRESS, 8);
        when(tournamentRepository.findById(TOURNAMENT_ID)).thenReturn(Optional.of(tournament));
        when(participantRepository.findByTournamentId(TOURNAMENT_ID)).thenReturn(participants(4));

        assertThrows(TournamentNotReadyToStartException.class,
                () -> tournamentGenerator.generate(TOURNAMENT_ID));

        verifyNoInteractions(phaseGenerator, roundGenerator, phaseMatchGenerator);
        verify(tournamentRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }
}