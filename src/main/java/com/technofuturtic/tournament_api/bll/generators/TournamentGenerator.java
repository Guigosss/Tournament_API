package com.technofuturtic.tournament_api.bll.generators;

import com.technofuturtic.tournament_api.bll.exceptions.engine.generator.MatchGeneratorNotFoundException;
import com.technofuturtic.tournament_api.bll.exceptions.engine.tournament.TournamentMustHaveParticipantsException;
import com.technofuturtic.tournament_api.bll.exceptions.engine.tournament.TournamentNotFoundException;
import com.technofuturtic.tournament_api.bll.exceptions.engine.tournament.TournamentNotReadyToStartException;
import com.technofuturtic.tournament_api.bll.exceptions.engine.tournament.TournamentParticipantLimitExceededException;
import com.technofuturtic.tournament_api.dal.repositories.ParticipantRepository;
import com.technofuturtic.tournament_api.dal.repositories.TournamentRepository;
import com.technofuturtic.tournament_api.dl.entities.TournamentEntity;
import com.technofuturtic.tournament_api.dl.entities.ParticipantEntity;
import com.technofuturtic.tournament_api.dl.entities.PhaseEntity;
import com.technofuturtic.tournament_api.dl.entities.RoundEntity;
import com.technofuturtic.tournament_api.dl.enums.PhaseType;
import com.technofuturtic.tournament_api.dl.enums.TournamentStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
public class TournamentGenerator {

    private final TournamentRepository tournamentRepository;
    private final ParticipantRepository participantRepository;

    private final PhaseGenerator phaseGenerator;
    private final RoundGenerator roundGenerator;
    private final List<MatchGenerator> matchGenerators;

    @Transactional
    public void generate(Integer tournamentId)  {
        TournamentEntity tournament = getTournament(tournamentId);
        List<ParticipantEntity> participants = getParticipants(tournamentId);

        validate(tournament, participants);

        //- Generate Phases
        List<PhaseEntity> phases = phaseGenerator.generate(tournament);

        //- Generate Rounds and Matches
        for (PhaseEntity phase : phases) {
            List<RoundEntity> rounds = roundGenerator.generate(phase, participants.size());
            MatchGenerator generator = getGenerator(phase.getType());
            generator.generate(rounds, participants);
        }

        tournament.setStatus(TournamentStatus.IN_PROGRESS);
        tournamentRepository.save(tournament);
    }

    private void validate(TournamentEntity tournament, List<ParticipantEntity> participants){
        if (participants == null || participants.isEmpty()) {
            throw new TournamentMustHaveParticipantsException();
        }

        if (participants.size() > tournament.getMaxParticipants()) {
            throw new TournamentParticipantLimitExceededException();
        }

        if (tournament.getStatus() != TournamentStatus.REGISTRATION_CLOSED) {
            throw new TournamentNotReadyToStartException(tournament.getId(), tournament.getStatus());
        }
    }

    private TournamentEntity getTournament(Integer tournamentId) {
        return tournamentRepository.findById(tournamentId)
                .orElseThrow(TournamentNotFoundException::new);
    }

    private List<ParticipantEntity> getParticipants(Integer tournamentId) {
        return participantRepository.findByTournamentId(tournamentId);
    }

    private MatchGenerator getGenerator(PhaseType phaseType) {
        return matchGenerators.stream()
                .filter(generator -> generator.supportedType() == phaseType)
                .findFirst()
                .orElseThrow(MatchGeneratorNotFoundException::new);
    }
}
