package com.technofuturtic.tournament_api.bll.exceptions.registration;

import com.technofuturtic.tournament_api.bll.exceptions.TournamentApiException;
import org.springframework.http.HttpStatus;

public abstract class RegistrationException extends TournamentApiException {

    /**
     * @param status code HTTP
     * @param body message/détails pour le client
     */
    public RegistrationException(HttpStatus status, Object body) {
        super(status, body, "user");
    }
}


