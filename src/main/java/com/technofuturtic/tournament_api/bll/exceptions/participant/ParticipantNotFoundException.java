package com.technofuturtic.tournament_api.bll.exceptions.participant;

import org.springframework.http.HttpStatus;


public class ParticipantNotFoundException extends ParticipantException {

    public ParticipantNotFoundException() {
        super(HttpStatus.NOT_FOUND, "User not found");
    }

    public ParticipantNotFoundException(String body) {
        super(HttpStatus.NOT_FOUND, body);
    }

    @Override
    public String toString() {
        return getBody().toString();
    }
}

