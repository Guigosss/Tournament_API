package com.technofuturtic.tournament_api.bll.generators;

import com.technofuturtic.tournament_api.dal.repositories.ParticipantRepository;
import com.technofuturtic.tournament_api.dal.repositories.TournamentRepository;
import com.technofuturtic.tournament_api.dl.entities.ParticipantEntity;
import com.technofuturtic.tournament_api.dl.entities.PhaseEntity;
import com.technofuturtic.tournament_api.dl.entities.TournamentEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class TournamentGenerator {

    private final TournamentRepository tournamentRepository;
    private final ParticipantRepository participantRepository;

    private final PhaseGenerator phaseGenerator;
    private final RoundGenerator roundGenerator;

    public void generate(Integer tournamentId)  {
        TournamentEntity tournament = getTournament(tournamentId);
        List<ParticipantEntity> participants = getParticipants(tournamentId);

        if (participants == null || participants.isEmpty()) {
            throw new IllegalStateException("The tournament must have at least one participant.");
        }

        if (participants.size() > tournament.getMaxParticipants()) {
            throw new IllegalStateException("The number of participants exceeds the tournament limit.");
        }

        //- Generate Phases
        List<PhaseEntity> phases = phaseGenerator.generate(tournament);

        //- Generate Rounds
        for (PhaseEntity phase : phases) {
            roundGenerator.generate(phase, participants.size());
        }
    }

    private TournamentEntity getTournament(Integer tournamentId) {
        return tournamentRepository.findById(tournamentId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Tournament not found"));
    }

    private List<ParticipantEntity> getParticipants(Integer tournamentId) {
        return participantRepository.findByTournamentId(tournamentId);
    }

}
