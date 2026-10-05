package com.technofuturtic.tournament_api.api.models.tournament.responses;

import com.technofuturtic.tournament_api.dl.entities.TournamentEntity;
import com.technofuturtic.tournament_api.dl.enums.ParticipantType;
import com.technofuturtic.tournament_api.dl.enums.TournamentFormat;
import com.technofuturtic.tournament_api.dl.enums.TournamentStatus;

import java.time.LocalDate;

public record TournamentResponse(
        Integer id,
        String name,
        String description,
        Integer maxParticipants,
        TournamentFormat format,
        TournamentStatus status,
        ParticipantType participantType,
        LocalDate startDate,
        LocalDate endDate,
        LocalDate registrationStartDate,
        LocalDate registrationEndDate,
        Integer organizerId,
        String organizerUsername
) {
    public static TournamentResponse fromEntity(TournamentEntity t){
        return new TournamentResponse(
                t.getId(), t.getName(), t.getDescription(), t.getMaxParticipants(),
                t.getFormat(), t.getStatus(), t.getParticipantType(),
                t.getStartDate(), t.getEndDate(),
                t.getRegistrationStartDate(), t.getRegistrationEndDate(),
                t.getOrganizer().getId(), t.getOrganizer().getUsername()
        );
    }
}
