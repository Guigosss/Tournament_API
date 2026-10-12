package com.technofuturtic.tournament_api.bll.exceptions.mail;

import com.technofuturtic.tournament_api.bll.exceptions.TournamentApiException;
import com.technofuturtic.tournament_api.dl.enums.Section;
import org.springframework.http.HttpStatus;

public abstract class MailException extends TournamentApiException {

    public MailException(HttpStatus status, Object body) {
        super(status, body, Section.MAIL.name());
    }
}
