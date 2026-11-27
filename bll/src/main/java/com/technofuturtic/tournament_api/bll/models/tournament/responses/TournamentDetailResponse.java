package com.technofuturtic.tournament_api.bll.models.tournament.responses;

import java.util.List;

public record TournamentDetailResponse(
        TournamentResponse tournament,
        ParticipantCountResponse participantCount,
        List<ParticipantResponse> participants
        //TODO Ajouter la liste des matchs (lecture seule) avec la Personne 3
) {
}