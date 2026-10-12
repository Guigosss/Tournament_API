package com.technofuturtic.tournament_api.bll.exceptions.mail;

import org.springframework.http.HttpStatus;

public class EmailSendingException extends MailException {

    public EmailSendingException() {
        super(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Error while sending the winner email"
        );
    }

    @Override
    public String toString() {
        return getBody().toString();
    }
}
