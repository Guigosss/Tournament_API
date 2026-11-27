package com.technofuturtic.tournament_api.bll.exceptions.team;

import org.springframework.http.HttpStatus;

public class TeamInvitationNotFoundException extends TeamException {
    public TeamInvitationNotFoundException() {
        super(HttpStatus.NOT_FOUND, "Invitation not found");
    }
}