package com.technofuturtic.tournament_api.bll.exceptions.engine.match;

import com.technofuturtic.tournament_api.bll.exceptions.engine.EngineException;
import org.springframework.http.HttpStatus;

public class TooManyParticipantsForBracketException extends EngineException {

    public TooManyParticipantsForBracketException(int participantCount, int bracketSize) {
        super(
                HttpStatus.BAD_REQUEST,
                "Too many participants (%d) for a bracket of %d."
                        .formatted(participantCount, bracketSize)
        );
    }

    @Override
    public String toString() {
        return getBody().toString();
    }
}
