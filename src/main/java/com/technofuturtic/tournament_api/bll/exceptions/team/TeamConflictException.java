package com.technofuturtic.tournament_api.bll.exceptions.team;

import com.technofuturtic.tournament_api.bll.exceptions.TournamentApiException;
import org.springframework.http.HttpStatus;
import java.util.Map;

public class TeamConflictException extends TournamentApiException {
    public TeamConflictException(String field, String message) {
        super(HttpStatus.CONFLICT, Map.of(field, message), "team");
    }
}