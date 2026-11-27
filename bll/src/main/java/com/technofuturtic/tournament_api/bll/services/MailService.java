package com.technofuturtic.tournament_api.bll.services;

public interface MailService {

    void sendWinnerEmail(String email, String winnerName, String tournamentName);
}
