package com.technofuturtic.tournament_api.bll.exceptions.engine;

import com.technofuturtic.tournament_api.bll.exceptions.TournamentApiException;
import com.technofuturtic.tournament_api.dl.enums.Section;
import org.springframework.http.HttpStatus;

public abstract class EngineException extends TournamentApiException {

    public EngineException(HttpStatus status, Object body) {
        super(status, body, Section.ENGINE.name());
    }
}
