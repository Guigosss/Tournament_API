package com.technofuturtic.tournament_api.bll.services;

import com.technofuturtic.tournament_api.api.models.tournament.requests.TournamentRequest;
import com.technofuturtic.tournament_api.api.models.tournament.responses.TournamentReponse;
import com.technofuturtic.tournament_api.dl.enums.TournamentStatus;

import java.util.List;

public interface TournamentService {

    TournamentReponse create (TournamentRequest request);

    void validateDates(TournamentRequest request);

    TournamentReponse getById(Integer id);

    List<TournamentReponse> getAll(TournamentStatus status);

    TournamentReponse update(Integer id, TournamentRequest request);

    TournamentReponse cancel(Integer id);
}
