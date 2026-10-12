package com.technofuturtic.tournament_api.bll.services.impls;

import com.technofuturtic.tournament_api.api.models.tournament.responses.ParticipantRankingResponse;
import com.technofuturtic.tournament_api.bll.exceptions.tournament.TournamentNotFoundException;
import com.technofuturtic.tournament_api.bll.services.RankingService;
import com.technofuturtic.tournament_api.dal.repositories.MatchRepository;
import com.technofuturtic.tournament_api.dal.repositories.ParticipantRepository;
import com.technofuturtic.tournament_api.dal.repositories.TournamentRepository;
import com.technofuturtic.tournament_api.dl.entities.MatchEntity;
import com.technofuturtic.tournament_api.dl.entities.ParticipantEntity;
import com.technofuturtic.tournament_api.dl.entities.TournamentEntity;
import com.technofuturtic.tournament_api.dl.enums.RoundType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Comparator;

@Service
@RequiredArgsConstructor
public class RankingServiceImpl implements RankingService {

    private final TournamentRepository tournamentRepository;
    private final ParticipantRepository participantRepository;
    private final MatchRepository matchRepository;

    @Override
    public List<ParticipantRankingResponse> getRanking(Integer tournamentId) {
        TournamentEntity tournament = tournamentRepository.findById(tournamentId)
                .orElseThrow(TournamentNotFoundException::new);

        List<ParticipantEntity> participants = participantRepository.findByTournamentId(tournament.getId());
        List<MatchEntity> matches = matchRepository.findByRoundPhaseTournamentId(tournament.getId());

        Map<Integer, Integer> rankByParticipantId = computeRanks(matches);

        List<ParticipantRankingResponse> ranking = new ArrayList<>();

        for (ParticipantEntity participant : participants) {
            Integer rank = rankByParticipantId.get(participant.getId());
            ranking.add(ParticipantRankingResponse.fromEntity(participant, rank));
        }

        ranking.sort(Comparator.comparing(ParticipantRankingResponse::rank));

        return ranking;
    }

    private Map<Integer, Integer> computeRanks(List<MatchEntity> matches) {
        Map<Integer, Integer> rankByParticipantId = new HashMap<>();

        for (MatchEntity match : matches) {

            if (match.getWinner() != null) {
                ParticipantEntity winner = match.getWinner();
                ParticipantEntity loser = winner.getId().equals(match.getParticipant1().getId())
                        ? match.getParticipant2()
                        : match.getParticipant1();

                RoundType roundType = match.getRound().getType();

                if (loser != null) {
                    rankByParticipantId.put(loser.getId(), roundType.getBracketSize() / 2 + 1);
                }

                if (roundType == RoundType.FINAL) {
                    rankByParticipantId.put(winner.getId(), 1);
                }
            }
        }

        return rankByParticipantId;
    }
}
