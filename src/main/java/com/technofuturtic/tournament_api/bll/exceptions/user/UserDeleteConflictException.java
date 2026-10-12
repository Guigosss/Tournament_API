package com.technofuturtic.tournament_api.bll.exceptions.user;

import org.springframework.http.HttpStatus;
import java.util.Map;

public class UserDeleteConflictException extends UserException {
    public UserDeleteConflictException() {
        super(HttpStatus.CONFLICT, Map.of("user", "Cannot delete a player linked to a team or tournament. Remove memberships, transfer captaincy or resolve tournament participation first."));
    }
}