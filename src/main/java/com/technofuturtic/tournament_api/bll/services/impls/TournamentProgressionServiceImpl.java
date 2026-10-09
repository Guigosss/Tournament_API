package com.technofuturtic.tournament_api.bll.services.impls;

import com.technofuturtic.tournament_api.bll.exceptions.engine.match.MatchNotFoundException;
import com.technofuturtic.tournament_api.bll.exceptions.mail.EmailSendingException;
import com.technofuturtic.tournament_api.bll.services.MailService;
import com.technofuturtic.tournament_api.bll.services.TournamentProgressionService;
import com.technofuturtic.tournament_api.dal.repositories.MatchRepository;
import com.technofuturtic.tournament_api.dal.repositories.RoundRepository;
import com.technofuturtic.tournament_api.dl.entities.MatchEntity;
import com.technofuturtic.tournament_api.dl.entities.ParticipantEntity;
import com.technofuturtic.tournament_api.dl.entities.RoundEntity;
import com.technofuturtic.tournament_api.dl.enums.MatchStatus;
import com.technofuturtic.tournament_api.dl.enums.RoundType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class TournamentProgressionServiceImpl implements TournamentProgressionService {

    private final MatchRepository matchRepository;
    private final RoundRepository roundRepository;

    private final MailService mailService;

    @Override
    public void progress(MatchEntity match) {

        RoundEntity currentRound = match.getRound();

        //- Final: no next round
        Optional<RoundEntity> nextRoundOpt = roundRepository.findByPhaseIdAndOrderIndex(currentRound.getPhase().getId(), currentRound.getOrderIndex() + 1);

        if (nextRoundOpt.isEmpty()) {
            notifyWinnerFinal(match);

        } else {
            RoundEntity nextRound = nextRoundOpt.get();

            //- Every two matches feed into the same match of the next round
            //- Matches 1-2 -> match 1, Matches 3-4 -> match 2, etc.
            int currentOrderIndex = (match.getOrderIndex() + 1) / 2;

            MatchEntity nextMatch = matchRepository.findByRoundIdAndOrderIndex(nextRound.getId(), currentOrderIndex).orElseThrow(MatchNotFoundException::new);

            //- Participant position in the next match
            if (match.getOrderIndex() % 2 == 1) {
                nextMatch.setParticipant1(match.getWinner());
            } else {
                nextMatch.setParticipant2(match.getWinner());
            }

            if (nextMatch.getParticipant1() != null && nextMatch.getParticipant2() != null) {
                nextMatch.setStatus(MatchStatus.PENDING);
            }

            matchRepository.save(nextMatch);
        }
    }

    private void notifyWinnerFinal(MatchEntity match) {
        if (match.getRound().getType() != RoundType.FINAL) {
            return;
        }

        ParticipantEntity winner = match.getWinner();
        if (winner == null || winner.getUser() == null) {
            return;
        }

        String tournamentName = winner.getTournament().getName();
        try {
            mailService.sendWinnerEmail(winner.getUser().getEmail(), winner.getUser().getUsername(), tournamentName);
        } catch (EmailSendingException e) {
            log.warn("Winner email not sent for tournament {}: {}", tournamentName, e.toString());
        }
    }
}
