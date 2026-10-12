package com.technofuturtic.tournament_api.bll.services.impls;

import com.technofuturtic.tournament_api.api.models.tournament.requests.MatchResultRequest;
import com.technofuturtic.tournament_api.api.models.tournament.responses.MatchResponse;
import com.technofuturtic.tournament_api.bll.exceptions.engine.match.InvalidMatchStatusException;
import com.technofuturtic.tournament_api.bll.exceptions.engine.match.InvalidMatchWinnerException;
import com.technofuturtic.tournament_api.bll.exceptions.engine.match.MatchNotFoundException;
import com.technofuturtic.tournament_api.bll.services.MatchService;
import com.technofuturtic.tournament_api.bll.services.TournamentProgressionService;
import com.technofuturtic.tournament_api.dal.repositories.MatchRepository;
import com.technofuturtic.tournament_api.dal.repositories.ParticipantRepository;
import com.technofuturtic.tournament_api.dl.entities.MatchEntity;
import com.technofuturtic.tournament_api.dl.entities.ParticipantEntity;
import com.technofuturtic.tournament_api.dl.enums.MatchStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MatchServiceImpl implements MatchService {

    private final MatchRepository matchRepository;
    private final ParticipantRepository participantRepository;
    private final TournamentProgressionService tournamentProgressionService;

    @Override
    @Transactional
    public MatchResponse submitResult(Integer matchId, MatchResultRequest request) {

        MatchEntity match = matchRepository.findById(matchId).orElseThrow(MatchNotFoundException::new);

        if (match.getStatus() != MatchStatus.PENDING) {
            throw new InvalidMatchStatusException();
        }

        ParticipantEntity winner = getWinner(match, request.winnerId());

        match.setScore1(request.score1());
        match.setScore2(request.score2());
        match.setWinner(winner);
        match.setStatus(MatchStatus.FINISHED);

        matchRepository.save(match);

        tournamentProgressionService.progress(match);

        return MatchResponse.fromEntity(match);
    }

    private ParticipantEntity getWinner(MatchEntity match, Integer winnerId) {
        if (!winnerId.equals(match.getParticipant1().getId())
                && !winnerId.equals(match.getParticipant2().getId())) {
            throw new InvalidMatchWinnerException();
        }
        return participantRepository.findById(winnerId).orElseThrow();
    }
}
