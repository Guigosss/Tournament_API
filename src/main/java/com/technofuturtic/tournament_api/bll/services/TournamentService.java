package com.technofuturtic.tournament_api.bll.services;

import com.technofuturtic.tournament_api.api.models.tournament.requests.TournamentRequest;
import com.technofuturtic.tournament_api.api.models.tournament.responses.TournamentResponse;
import com.technofuturtic.tournament_api.dl.enums.TournamentStatus;

import java.util.List;

public interface TournamentService {

    TournamentResponse create (TournamentRequest request);

    TournamentResponse getById(Integer id);

    List<TournamentResponse> getAll(TournamentStatus status);

    TournamentResponse update(Integer id, TournamentRequest request);

    TournamentResponse cancel(Integer id);

    TournamentResponse changeStatus(Integer id, TournamentStatus newStatus);

}
