package com.technofuturtic.tournament_api.bll.exceptions.engine.tournament;

import com.technofuturtic.tournament_api.bll.exceptions.engine.EngineException;
import org.springframework.http.HttpStatus;

public class TournamentNotFoundException extends EngineException {

    public TournamentNotFoundException() {
        super(
                HttpStatus.NOT_FOUND,
                "Tournament not found."
        );
    }

    @Override
    public String toString() {
        return getBody().toString();
    }
}
