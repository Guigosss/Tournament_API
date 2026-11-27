package com.technofuturtic.tournament_api.bll.exceptions.engine.round;

import com.technofuturtic.tournament_api.bll.exceptions.engine.EngineException;
import org.springframework.http.HttpStatus;

public class EliminationParticipantCountOutOfRangeException extends EngineException {

    public EliminationParticipantCountOutOfRangeException(int minParticipants, int maxParticipants) {
        super(
                HttpStatus.BAD_REQUEST,
                "An elimination phase requires between %d and %d participants."
                        .formatted(minParticipants, maxParticipants)
        );
    }

    @Override
    public String toString() {
        return getBody().toString();
    }
}