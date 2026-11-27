package com.technofuturtic.tournament_api.bll.exceptions.engine.match;

import com.technofuturtic.tournament_api.bll.exceptions.engine.EngineException;
import org.springframework.http.HttpStatus;

public class InvalidMatchStatusException extends EngineException {

    public InvalidMatchStatusException() {
        super(
                HttpStatus.BAD_REQUEST,
                "Match status does not allow result submission."
        );
    }

    @Override
    public String toString() {
        return getBody().toString();
    }
}
