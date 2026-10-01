package com.technofuturtic.tournament_api.bll.exceptions.engine.tournament;

import com.technofuturtic.tournament_api.bll.exceptions.engine.EngineException;
import org.springframework.http.HttpStatus;

public class TournamentParticipantLimitExceededException extends EngineException {

    public TournamentParticipantLimitExceededException() {
        super(
                HttpStatus.CONFLICT,
                "The number of participants exceeds the tournament limit."
        );
    }

    @Override
    public String toString() {
        return getBody().toString();
    }
}
