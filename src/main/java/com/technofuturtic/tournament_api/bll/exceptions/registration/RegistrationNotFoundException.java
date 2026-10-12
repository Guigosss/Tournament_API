package com.technofuturtic.tournament_api.bll.exceptions.registration;

import org.springframework.http.HttpStatus;


public class RegistrationNotFoundException extends RegistrationException {

    public RegistrationNotFoundException() {
        super(HttpStatus.NOT_FOUND, "User not found");
    }

    /**
     * @param body message d'erreur personnalisé
     */
    public RegistrationNotFoundException(String body) {
        super(HttpStatus.NOT_FOUND, body);
    }

    @Override
    public String toString() {
        return getBody().toString();
    }
}

