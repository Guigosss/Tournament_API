package com.technofuturtic.tournament_api.bll.services;

import com.technofuturtic.tournament_api.api.models.tournament.responses.ParticipantCountResponse;
import com.technofuturtic.tournament_api.api.models.tournament.responses.ParticipantResponse;

import java.util.List;

public interface ParticipantService {

        List<ParticipantResponse> getByTournament(Integer tournamentId);

        ParticipantResponse getById(Integer tournamentId, Integer participantId);

        List<ParticipantResponse> getByTournamentIdAndTeamId(Integer tournamentId, Integer teamId);

        ParticipantCountResponse count(Integer tournamentId);
}
