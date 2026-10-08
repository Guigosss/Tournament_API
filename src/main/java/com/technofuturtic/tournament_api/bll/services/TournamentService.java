package com.technofuturtic.tournament_api.bll.services;

import com.technofuturtic.tournament_api.api.models.tournament.requests.TournamentRequest;
import com.technofuturtic.tournament_api.api.models.tournament.requests.TournamentUpdateRequest;
import com.technofuturtic.tournament_api.api.models.tournament.responses.TournamentDetailResponse;
import com.technofuturtic.tournament_api.api.models.tournament.responses.TournamentResponse;
import com.technofuturtic.tournament_api.dl.enums.TournamentStatus;

import java.util.List;

public interface TournamentService {

    TournamentResponse create(TournamentRequest request);

    TournamentResponse getById(Integer id);

    TournamentDetailResponse getDetail(Integer id);

    List<TournamentResponse> getAll(TournamentStatus status);

    TournamentResponse update(Integer id, TournamentUpdateRequest request);

    TournamentResponse cancel(Integer id);

    TournamentResponse changeStatus(Integer id, TournamentStatus newStatus);

    TournamentResponse changeOrganizer(Integer id, Integer newOrganizerId);
}