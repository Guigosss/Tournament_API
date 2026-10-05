package com.technofuturtic.tournament_api.bll.exceptions.team;

import com.technofuturtic.tournament_api.bll.exceptions.tournament.TournamentException;
import org.springframework.http.HttpStatus;


public class TeamNotFoundException extends TournamentException {

    public TeamNotFoundException() {
        super(HttpStatus.NOT_FOUND, "User not found");
    }

    /**
     * @param body message d'erreur personnalisé
     */
    public TeamNotFoundException(String body) {
        super(HttpStatus.NOT_FOUND, body);
    }

    @Override
    public String toString() {
        return getBody().toString();
    }
}
