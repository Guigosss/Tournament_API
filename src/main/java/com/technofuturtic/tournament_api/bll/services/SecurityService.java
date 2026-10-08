package com.technofuturtic.tournament_api.bll.services;

public interface SecurityService {

    boolean isTournamentOrganizer(Integer tournamentId);
}