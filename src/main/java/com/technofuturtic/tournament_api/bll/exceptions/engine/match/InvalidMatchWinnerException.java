package com.technofuturtic.tournament_api.bll.exceptions.engine.match;

import com.technofuturtic.tournament_api.bll.exceptions.engine.EngineException;
import org.springframework.http.HttpStatus;

public class InvalidMatchWinnerException extends EngineException {

    public InvalidMatchWinnerException() {
        super(
                HttpStatus.BAD_REQUEST,
                "The selected winner is not a participant of the match."
        );
    }

    @Override
    public String toString() {
        return getBody().toString();
    }
}
