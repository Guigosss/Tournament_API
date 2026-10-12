package com.technofuturtic.tournament_api.bll.generators.rules;

public final class EliminationRules {

    public static final int MIN_PARTICIPANTS = 2;
    public static final int MAX_PARTICIPANTS = 128;

    private EliminationRules() {}

    public static int bracketSize(int participantCount) {
        return (int) Math.pow(2, Math.ceil(Math.log(participantCount) / Math.log(2)));
    }
}
