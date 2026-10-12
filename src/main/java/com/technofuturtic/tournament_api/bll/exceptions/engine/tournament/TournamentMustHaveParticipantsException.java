package com.technofuturtic.tournament_api.bll.exceptions.engine.tournament;

import com.technofuturtic.tournament_api.bll.exceptions.engine.EngineException;
import org.springframework.http.HttpStatus;

public class TournamentMustHaveParticipantsException extends EngineException {

    public TournamentMustHaveParticipantsException() {
        super(
                HttpStatus.BAD_REQUEST,
                "The tournament must have at least one participant."
        );
    }

    @Override
    public String toString() {
        return getBody().toString();
    }
}
