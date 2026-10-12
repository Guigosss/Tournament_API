package com.technofuturtic.tournament_api.dl.enums;

public enum TournamentStatus {
    UPCOMING,
    REGISTRATION_OPEN,
    REGISTRATION_CLOSED,
    IN_PROGRESS,
    FINISHED,
    CANCELED;

    public boolean canTransitionTo(TournamentStatus next) {
        return switch (this) {
            case UPCOMING -> next == REGISTRATION_OPEN || next == CANCELED;
            case REGISTRATION_OPEN -> next == REGISTRATION_CLOSED || next == CANCELED;
            case REGISTRATION_CLOSED -> next == REGISTRATION_OPEN
                    || next == IN_PROGRESS || next == CANCELED;
            case IN_PROGRESS -> next == FINISHED;
            case FINISHED, CANCELED -> false;
        };
    }

}
