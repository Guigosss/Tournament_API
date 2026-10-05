package com.technofuturtic.tournament_api.api.models.tournament.requests;

import com.technofuturtic.tournament_api.dl.enums.ParticipantType;
import com.technofuturtic.tournament_api.dl.enums.TournamentFormat;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record TournamentRequest(
        @NotBlank String name,
        @NotBlank String description,
        @NotNull @Min(2) Integer maxParticipants,
        @NotNull TournamentFormat format,
        @NotNull ParticipantType participantType,
        @NotNull LocalDate startDate,
        @NotNull LocalDate endDate,
        @NotNull LocalDate registrationStartDate,
        @NotNull LocalDate registrationEndDate,
        @NotNull Integer organizerId
) {
}
