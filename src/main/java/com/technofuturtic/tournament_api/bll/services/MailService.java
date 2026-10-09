package com.technofuturtic.tournament_api.bll.services;

import org.springframework.messaging.MessagingException;

public interface MailService {

    void envoyerMailGagnant(
            String email,
            String nomGagnant,
            String nomTournoi
    ) throws MessagingException;
}
