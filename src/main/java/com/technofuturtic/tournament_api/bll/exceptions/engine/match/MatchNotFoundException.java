package com.technofuturtic.tournament_api.bll.exceptions.engine.match;

import com.technofuturtic.tournament_api.bll.exceptions.engine.EngineException;
import org.springframework.http.HttpStatus;

public class MatchNotFoundException extends EngineException {

    public MatchNotFoundException() {
        super(
                HttpStatus.NOT_FOUND,
                "Match not found."
        );
    }

    @Override
    public String toString() {
        return getBody().toString();
    }
}
