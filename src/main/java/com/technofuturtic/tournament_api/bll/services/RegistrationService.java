package com.technofuturtic.tournament_api.bll.services;

import com.technofuturtic.tournament_api.api.models.tournament.responses.RegistrationCheckResponse;
import com.technofuturtic.tournament_api.api.models.tournament.responses.RegistrationResponse;
import com.technofuturtic.tournament_api.dl.enums.RegistrationStatus;

import java.util.List;

public interface RegistrationService {

    RegistrationResponse registrationPlayer(Integer tournamentId, Integer userId);

    RegistrationResponse registrationTeam(Integer tournamentId, Integer teamId);

    void unregisterPlayer(Integer tournamentId, Integer userId);

    void unregisterTeam(Integer tournamentId, Integer teamId);

    RegistrationResponse validatePlayer(Integer tournamentId, Integer userId);

    RegistrationResponse validateTeam(Integer tournamentId, Integer teamId);

    RegistrationResponse excludePlayer(Integer tournamentId, Integer userId);

    RegistrationResponse excludeTeam(Integer tournamentId, Integer teamId);

    List<RegistrationResponse> getRegistrations(Integer tournamentId, RegistrationStatus status);

    RegistrationCheckResponse checkPlayerRegistration(Integer tournamentId, Integer userId);

    RegistrationCheckResponse checkTeamRegistration(Integer tournamentId, Integer teamId);
}
