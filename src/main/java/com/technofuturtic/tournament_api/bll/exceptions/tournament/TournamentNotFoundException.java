package com.technofuturtic.tournament_api.bll.exceptions.tournament;

import org.springframework.http.HttpStatus;

public class TournamentNotFoundException extends TournamentException {

    public TournamentNotFoundException() {
        super(HttpStatus.NOT_FOUND, "User not found");
    }

    /**
     * @param body message d'erreur personnalisé
     */
    public TournamentNotFoundException(String body) {
        super(HttpStatus.NOT_FOUND, body);
    }

    @Override
    public String toString() {
        return getBody().toString();
    }
}
