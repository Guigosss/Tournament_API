package com.technofuturtic.tournament_api.api.models.tournament.responses;

import com.technofuturtic.tournament_api.api.models.tournament.requests.TeamRegistrationRequest;
import com.technofuturtic.tournament_api.dl.entities.RegisterTeamEntity;
import com.technofuturtic.tournament_api.dl.entities.RegisterUserEntity;
import com.technofuturtic.tournament_api.dl.enums.RegistrationStatus;

import java.time.LocalDateTime;

public record RegistrationReponse(
        Integer id,
        Integer tournamentId,
        Integer userId,
        String username,
        Integer teamId,
        String teamName,
        LocalDateTime registerDate,
        RegistrationStatus status
) {
    public static RegistrationReponse fromUserRegistration(RegisterUserEntity r) {
        return new RegistrationReponse(
                r.getId(),
                r.getTournament().getId(),
                r.getUser().getId(),
                r.getUser().getUsername(),
                null,
                null,
                r.getRegisterDate(),
                r.getStatus()

        );
    }

    public static RegistrationReponse fromTeamRegistration(RegisterTeamEntity r) {
        return new RegistrationReponse(
                r.getId(),
                r.getTournament().getId(),
                null,
                null,
                r.getTeam().getId(),
                r.getTeam().getName(),
                r.getRegisterDate(),
                r.getStatus()
        );
    }
}
