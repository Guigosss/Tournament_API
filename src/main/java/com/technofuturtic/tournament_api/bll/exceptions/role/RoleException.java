package com.technofuturtic.tournament_api.bll.exceptions.role;

import com.technofuturtic.tournament_api.bll.exceptions.TournamentApiException;
import org.springframework.http.HttpStatus;

public abstract class RoleException extends TournamentApiException {

    public RoleException(HttpStatus status, Object body) {
        super(status, body, "role");
    }
}
