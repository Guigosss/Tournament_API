package com.technofuturtic.tournament_api.bll.services;

import org.springframework.messaging.MessagingException;

public interface MailService {

    void sendWinnerEmail(String email, String winnerName, String tournamentName) throws MessagingException;
}
