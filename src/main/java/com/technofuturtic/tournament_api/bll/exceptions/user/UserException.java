package com.technofuturtic.tournament_api.bll.exceptions.user;

import com.technofuturtic.tournament_api.bll.exceptions.IntroSpringApiException;
import org.springframework.http.HttpStatus;

public abstract class UserException extends IntroSpringApiException {

    public UserException(HttpStatus status, Object body) {
        super(status, body, "user");
    }
}
