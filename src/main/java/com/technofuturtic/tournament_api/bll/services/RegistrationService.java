package com.technofuturtic.tournament_api.bll.services;

import com.technofuturtic.tournament_api.api.models.tournament.responses.RegistrationResponse;

public interface RegistrationService {

    RegistrationResponse registrationPlayer(Integer tournamentId, Integer userId);

    RegistrationResponse registrationTeam(Integer tournamentId, Integer teamId);

    void unregisterPlayer(Integer tournamentId, Integer userId);

    void unregisterTeam(Integer tournamentId, Integer teamId);

    RegistrationResponse validatePlayer(Integer tournamentId, Integer userId);

    RegistrationResponse validateTeam(Integer tournamentId, Integer teamId);

    RegistrationResponse excludePlayer(Integer tournamentId, Integer userId);

    RegistrationResponse excludeTeam(Integer tournamentId, Integer teamId);
}
