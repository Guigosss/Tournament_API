package com.technofuturtic.tournament_api.bll.exceptions.engine.generator;

import com.technofuturtic.tournament_api.bll.exceptions.engine.EngineException;
import org.springframework.http.HttpStatus;

public class MatchGeneratorNotFoundException extends EngineException {

    public MatchGeneratorNotFoundException() {
        super(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "No match generator found for the specified phase type."
        );
    }

    @Override
    public String toString() { return getBody().toString(); }
}
