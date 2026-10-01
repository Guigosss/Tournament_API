package com.technofuturtic.tournament_api.bll.exceptions.user;

import com.technofuturtic.tournament_api.bll.exceptions.TournamentApiException;
import org.springframework.http.HttpStatus;

/**
 * Classe de base pour les exceptions métier concernant les Users.
 * Scopes les exceptions à la section "user".
 */
public abstract class UserException extends TournamentApiException {

    /**
     * @param status code HTTP
     * @param body message/détails pour le client
     */
    public UserException(HttpStatus status, Object body) {
        super(status, body, "user");
    }
}
