package com.technofuturtic.tournament_api.bll.exceptions.tournament;

import com.technofuturtic.tournament_api.bll.exceptions.TournamentApiException;
import org.springframework.http.HttpStatus;

public abstract class TournamentException extends TournamentApiException {

    /**
     * @param status code HTTP
     * @param body message/détails pour le client
     */
    public TournamentException(HttpStatus status, Object body) {
        super(status, body, "user");
    }
}
