package com.technofuturtic.tournament_api.bll.exceptions.participant;

import com.technofuturtic.tournament_api.bll.exceptions.TournamentApiException;
import org.springframework.http.HttpStatus;


public abstract class ParticipantException extends TournamentApiException {

    public ParticipantException(HttpStatus status, Object body) {
        super(status, body, "user");
    }
}
