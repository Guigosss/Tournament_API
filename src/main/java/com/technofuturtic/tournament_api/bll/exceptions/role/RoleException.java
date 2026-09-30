package com.technofuturtic.tournament_api.bll.exceptions.role;

import com.technofuturtic.tournament_api.bll.exceptions.IntroSpringApiException;
import org.springframework.http.HttpStatus;

public abstract class RoleException extends IntroSpringApiException {

    public RoleException(HttpStatus status, Object body) {
        super(status, body, "role");
    }
}
