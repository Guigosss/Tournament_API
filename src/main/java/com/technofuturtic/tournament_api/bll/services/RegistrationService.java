package com.technofuturtic.tournament_api.bll.services;

import com.technofuturtic.tournament_api.api.models.tournament.responses.RegistrationReponse;

public interface RegistrationService {

    RegistrationReponse registrationPlayer(Integer tournamentId, Integer userId);

    RegistrationReponse registrationTeam(Integer tournamentId, Integer teamId);

    void unregisterPlayer(Integer tournamentId, Integer userId);

    void unregisterTeam(Integer tournamentId, Integer teamId);


}
