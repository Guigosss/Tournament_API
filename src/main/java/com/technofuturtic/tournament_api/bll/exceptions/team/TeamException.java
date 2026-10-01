package com.technofuturtic.tournament_api.bll.exceptions.team;

import com.technofuturtic.tournament_api.bll.exceptions.TournamentApiException;
import org.springframework.http.HttpStatus;


public abstract class TeamException extends TournamentApiException {

    /**
     * @param status code HTTP
     * @param body message/détails pour le client
     */
    public TeamException(HttpStatus status, Object body) {
        super(status, body, "user");
    }
}

