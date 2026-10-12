package com.technofuturtic.tournament_api.bll.services.impls;

import com.technofuturtic.tournament_api.api.models.tournament.responses.MatchResponse;
import com.technofuturtic.tournament_api.api.models.tournament.responses.PhaseBracketResponse;
import com.technofuturtic.tournament_api.api.models.tournament.responses.TournamentBracketResponse;
import com.technofuturtic.tournament_api.api.models.tournament.responses.PhaseResponse;
import com.technofuturtic.tournament_api.api.models.tournament.responses.RoundResponse;
import com.technofuturtic.tournament_api.api.models.tournament.responses.RoundBracketResponse;
import com.technofuturtic.tournament_api.bll.exceptions.engine.tournament.TournamentNotFoundException;
import com.technofuturtic.tournament_api.bll.services.TournamentGenerationService;
import com.technofuturtic.tournament_api.dal.repositories.MatchRepository;
import com.technofuturtic.tournament_api.dal.repositories.PhaseRepository;
import com.technofuturtic.tournament_api.dal.repositories.RoundRepository;
import com.technofuturtic.tournament_api.dal.repositories.TournamentRepository;
import com.technofuturtic.tournament_api.dl.entities.PhaseEntity;
import com.technofuturtic.tournament_api.dl.entities.RoundEntity;
import com.technofuturtic.tournament_api.dl.entities.TournamentEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TournamentGenerationServiceImpl implements TournamentGenerationService {

    private final TournamentRepository tournamentRepository;
    private final PhaseRepository phaseRepository;
    private final RoundRepository roundRepository;
    private final MatchRepository matchRepository;

    @Override
    public TournamentBracketResponse getBracket(Integer tournamentId) {
        TournamentEntity tournament = tournamentRepository.findById(tournamentId).orElseThrow(TournamentNotFoundException::new);

        List<PhaseBracketResponse> phases = phaseRepository.findByTournamentIdOrderByOrderIndexAsc(tournament.getId())
                .stream()
                .map(this::toPhaseBracketResponse)
                .toList();

        return TournamentBracketResponse.fromEntity(tournament, phases);
    }

    @Override
    public List<PhaseResponse> getPhases(Integer tournamentId) {
        return phaseRepository.findByTournamentIdOrderByOrderIndexAsc(tournamentId)
                .stream()
                .map(PhaseResponse::fromEntity)
                .toList();
    }

    @Override
    public List<RoundResponse> getRounds(Integer tournamentId, Integer phaseId) {
        return roundRepository.findByTournamentAndPhase(tournamentId, phaseId)
                .stream()
                .map(RoundResponse::fromEntity)
                .toList();
    }

    @Override
    public List<MatchResponse> getMatches(Integer tournamentId, Integer phaseId, Integer roundId) {
        return matchRepository.findByTournamentPhaseAndRound(tournamentId, phaseId, roundId)
                .stream()
                .map(MatchResponse::fromEntity)
                .toList();
    }

    private PhaseBracketResponse toPhaseBracketResponse(PhaseEntity phase) {
        List<RoundBracketResponse> rounds = roundRepository.findByPhaseIdOrderByOrderIndex(phase.getId())
                .stream()
                .map(this::toRoundBracketResponse)
                .toList();

        return PhaseBracketResponse.fromEntity(phase, rounds);
    }

    private RoundBracketResponse toRoundBracketResponse(RoundEntity round) {
        List<MatchResponse> matches = matchRepository.findByRoundId(round.getId())
                .stream()
                .map(MatchResponse::fromEntity)
                .toList();

        return RoundBracketResponse.fromEntity(round, matches);
    }
}
