package com.technofuturtic.tournament_api.bll.exceptions;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.springframework.http.HttpStatus;

@EqualsAndHashCode(callSuper = false) @ToString
public abstract class TournamentApiException extends RuntimeException {

    @Getter
    private String section;

    @Getter
    private final HttpStatus status;

    @Getter
    private final Object body;

    /**
     * Constructeur parent.
     * @param status code HTTP de réponse
     * @param body contenu du body pour le client
     * @param section section métier concernée (user, game, etc.)
     */
    public TournamentApiException(HttpStatus status, Object body, String section) {
        super();
        this.status = status;
        this.body = body;
        this.section = section;
    }
}
