package com.technofuturtic.tournament_api.bll.exceptions.user;

import com.technofuturtic.tournament_api.bll.exceptions.TournamentApiException;
import org.springframework.http.HttpStatus;

public abstract class UserException extends TournamentApiException {

    public UserException(HttpStatus status, Object body) {
        super(status, body, "user");
    }
}
