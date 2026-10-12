package com.technofuturtic.tournament_api.bll.services.impls;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import com.technofuturtic.tournament_api.api.models.tournament.responses.ParticipantRankingResponse;
import com.technofuturtic.tournament_api.bll.exceptions.engine.tournament.TournamentNotFoundException;
import com.technofuturtic.tournament_api.dal.repositories.MatchRepository;
import com.technofuturtic.tournament_api.dal.repositories.ParticipantRepository;
import com.technofuturtic.tournament_api.dal.repositories.TournamentRepository;
import com.technofuturtic.tournament_api.dl.entities.MatchEntity;
import com.technofuturtic.tournament_api.dl.entities.ParticipantEntity;
import com.technofuturtic.tournament_api.dl.entities.RoundEntity;
import com.technofuturtic.tournament_api.dl.entities.TournamentEntity;
import com.technofuturtic.tournament_api.dl.enums.RoundType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("RankingServiceImpl")
class RankingServiceImplTest {

    private static final Integer TOURNAMENT_ID = 1;

    @Mock
    private TournamentRepository tournamentRepository;

    @Mock
    private ParticipantRepository participantRepository;

    @Mock
    private MatchRepository matchRepository;

    @InjectMocks
    private RankingServiceImpl rankingService;

    //- Helpers
    private ParticipantEntity participant(int id) {
        ParticipantEntity participant = mock(ParticipantEntity.class);
        lenient().when(participant.getId()).thenReturn(id);
        return participant;
    }

    private MatchEntity match(RoundType roundType, ParticipantEntity participant1,
                              ParticipantEntity participant2, ParticipantEntity winner) {
        RoundEntity round = mock(RoundEntity.class);
        lenient().when(round.getType()).thenReturn(roundType);

        MatchEntity match = mock(MatchEntity.class);
        lenient().when(match.getRound()).thenReturn(round);
        lenient().when(match.getParticipant1()).thenReturn(participant1);
        lenient().when(match.getParticipant2()).thenReturn(participant2);
        lenient().when(match.getWinner()).thenReturn(winner);
        return match;
    }

    private void givenTournament(List<ParticipantEntity> participants, List<MatchEntity> matches) {
        TournamentEntity tournament = mock(TournamentEntity.class);
        when(tournament.getId()).thenReturn(TOURNAMENT_ID);

        when(tournamentRepository.findById(TOURNAMENT_ID)).thenReturn(Optional.of(tournament));
        when(participantRepository.findByTournamentId(TOURNAMENT_ID)).thenReturn(participants);
        when(matchRepository.findByRoundPhaseTournamentId(TOURNAMENT_ID)).thenReturn(matches);
    }

    @Test
    @DisplayName("getRanking : tournoi inexistant -> TournamentNotFoundException")
    void getRanking_tournamentNotFound_throws() {
        when(tournamentRepository.findById(TOURNAMENT_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> rankingService.getRanking(TOURNAMENT_ID))
                .isInstanceOf(TournamentNotFoundException.class);

        verifyNoInteractions(participantRepository, matchRepository);
    }

    @Test
    @DisplayName("getRanking : aucun participant -> liste vide")
    void getRanking_noParticipants_returnsEmptyList() {
        givenTournament(List.of(), List.of());

        assertThat(rankingService.getRanking(TOURNAMENT_ID)).isEmpty();
    }

    @Test
    @DisplayName("getRanking : tournoi terminé (4 joueurs) -> 1er, 2e, puis les deux demi-finalistes ex aequo 3e")
    void getRanking_finishedTournament_returnsFullRanking() {
        ParticipantEntity a = participant(1);
        ParticipantEntity b = participant(2);
        ParticipantEntity c = participant(3);
        ParticipantEntity d = participant(4);

        MatchEntity semi1 = match(RoundType.SEMI_FINAL, a, b, a);
        MatchEntity semi2 = match(RoundType.SEMI_FINAL, d, c, c);
        MatchEntity finale = match(RoundType.FINAL, a, c, a);

        givenTournament(List.of(a, b, c, d), List.of(semi1, semi2, finale));

        List<ParticipantRankingResponse> ranking = rankingService.getRanking(TOURNAMENT_ID);

        assertThat(ranking)
                .extracting(ParticipantRankingResponse::participantId, ParticipantRankingResponse::rank)
                .containsExactly(
                        tuple(1, 1),
                        tuple(3, 2),
                        tuple(2, 3),
                        tuple(4, 3)
                );
    }

    @Test
    @DisplayName("getRanking : finale seule -> vainqueur 1er, perdant 2e")
    void getRanking_finalOnly_winnerFirstLoserSecond() {
        ParticipantEntity a = participant(1);
        ParticipantEntity b = participant(2);

        givenTournament(List.of(a, b), List.of(match(RoundType.FINAL, a, b, a)));

        List<ParticipantRankingResponse> ranking = rankingService.getRanking(TOURNAMENT_ID);

        assertThat(ranking)
                .extracting(ParticipantRankingResponse::participantId, ParticipantRankingResponse::rank)
                .containsExactly(tuple(1, 1), tuple(2, 2));
    }

    @ParameterizedTest(name = "perdant en {0} -> rang {1}")
    @CsvSource({
            "SEMI_FINAL, 3",
            "QUARTER_FINAL, 5",
            "ROUND_OF_16, 9"
    })
    @DisplayName("getRanking : le rang du perdant dépend du round où il est éliminé")
    void getRanking_loserRankDependsOnRound(RoundType roundType, int expectedRank) {
        ParticipantEntity winner = participant(1);
        ParticipantEntity loser = participant(2);
        ParticipantEntity finalist = participant(3);

        MatchEntity testedMatch = match(roundType, winner, loser, winner);
        MatchEntity finale = match(RoundType.FINAL, winner, finalist, winner);

        givenTournament(List.of(winner, loser, finalist), List.of(testedMatch, finale));

        List<ParticipantRankingResponse> ranking = rankingService.getRanking(TOURNAMENT_ID);

        ParticipantRankingResponse loserResponse = ranking.stream()
                .filter(r -> r.participantId().equals(2))
                .findFirst()
                .orElseThrow();

        assertThat(loserResponse.rank()).isEqualTo(expectedRank);
    }

    @Test
    @DisplayName("getRanking : match sans adversaire (participant2 null) -> pas d'erreur")
    void getRanking_matchWithoutOpponent_participant2Null_doesNotFail() {
        ParticipantEntity a = participant(1);
        ParticipantEntity b = participant(2);

        MatchEntity noOpponent = match(RoundType.SEMI_FINAL, a, null, a);
        MatchEntity finale = match(RoundType.FINAL, a, b, a);

        givenTournament(List.of(a, b), List.of(noOpponent, finale));

        List<ParticipantRankingResponse> ranking = rankingService.getRanking(TOURNAMENT_ID);

        assertThat(ranking)
                .extracting(ParticipantRankingResponse::participantId, ParticipantRankingResponse::rank)
                .containsExactly(tuple(1, 1), tuple(2, 2));
    }
}