package com.technofuturtic.tournament_api.bll.exceptions.engine.tournament;

import com.technofuturtic.tournament_api.bll.exceptions.engine.EngineException;
import com.technofuturtic.tournament_api.dl.enums.TournamentStatus;
import org.springframework.http.HttpStatus;

public class TournamentNotReadyToStartException extends EngineException {

    public TournamentNotReadyToStartException(Integer tournamentId, TournamentStatus status) {
        super(
                HttpStatus.BAD_REQUEST,
                "Tournament with ID " + tournamentId
                        + " is not ready to start. Current status: " + status + "."
        );
    }

    @Override
    public String toString() {
        return getBody().toString();
    }
}



