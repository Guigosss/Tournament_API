package com.technofuturtic.tournament_api.api.models.tournament.responses;

import com.technofuturtic.tournament_api.dl.entities.ParticipantEntity;
import com.technofuturtic.tournament_api.dl.entities.RegisterTeamEntity;
import com.technofuturtic.tournament_api.dl.entities.RegisterUserEntity;
import com.technofuturtic.tournament_api.dl.enums.ParticipantType;
import com.technofuturtic.tournament_api.dl.enums.RegistrationStatus;

import java.time.LocalDateTime;

//public record ParticipantResponse(
//        Integer id,
//        Integer userId,
//        Integer teamId
//    ) {
//
//    public static ParticipantResponse fromEntity(ParticipantEntity participant) {
//        if (participant == null) {
//            return null;
//        }
//
//        return new ParticipantResponse(
//                participant.getId(),
//                participant.getUser() != null
//                        ? participant.getUser().getId()
//                        : null,
//                participant.getTeam() != null
//                        ? participant.getTeam().getId()
//                        : null
//        );
//    }
//}
//
//public record ParticipantResponse(
//        Integer id,
//        Integer tournamentId,
//        ParticipantType type,
//        Integer userId,
//        String username,
//        String email,
//        Integer teamId,
//        String teamName,
//        LocalDateTime registerDate,
//        RegistrationStatus status
//) {
//    public static ParticipantResponse fromUser(ParticipantEntity p, RegisterUserEntity r) {
//        return new ParticipantResponse(
//                p.getId(),
//                p.getTournament().getId(),
//                ParticipantType.PLAYER,
//                p.getUser().getId(),
//                p.getUser().getUsername(),
//                p.getUser().getEmail(),
//                null,
//                null,
//                r.getRegisterDate(),
//                r.getStatus()
//        );
//    }
//
//    public static ParticipantResponse fromTeam(ParticipantEntity p, RegisterTeamEntity r) {
//        return new ParticipantResponse(
//                p.getId(),
//                p.getTournament().getId(),
//                ParticipantType.TEAM,
//                null,
//                null,
//                null,
//                p.getTeam().getId(),
//                p.getTeam().getName(),
//                r.getRegisterDate(),
//                r.getStatus()
//        );
//    }
//}

public record ParticipantResponse(
        Integer id,
        Integer tournamentId,
        ParticipantType type,
        Integer userId,
        String username,
        String email,
        Integer teamId,
        String teamName,
        LocalDateTime registerDate,
        RegistrationStatus status
) {

    public static ParticipantResponse fromEntity(ParticipantEntity participant) {
        return new ParticipantResponse(
                participant.getId(),
                participant.getTournament().getId(),
                participant.getUser() != null ? ParticipantType.PLAYER : ParticipantType.TEAM,
                participant.getUser() != null ? participant.getUser().getId() : null,
                participant.getUser() != null ? participant.getUser().getUsername() : null,
                participant.getUser() != null ? participant.getUser().getEmail() : null,
                participant.getTeam() != null ? participant.getTeam().getId() : null,
                participant.getTeam() != null ? participant.getTeam().getName() : null,
                null,
                null
        );
    }

    public static ParticipantResponse fromUser(ParticipantEntity p, RegisterUserEntity r) {
        return new ParticipantResponse(
                p.getId(),
                p.getTournament().getId(),
                ParticipantType.PLAYER,
                p.getUser().getId(),
                p.getUser().getUsername(),
                p.getUser().getEmail(),
                null,
                null,
                r.getRegisterDate(),
                r.getStatus()
        );
    }

    public static ParticipantResponse fromTeam(ParticipantEntity p, RegisterTeamEntity r) {
        return new ParticipantResponse(
                p.getId(),
                p.getTournament().getId(),
                ParticipantType.TEAM,
                null,
                null,
                null,
                p.getTeam().getId(),
                p.getTeam().getName(),
                r.getRegisterDate(),
                r.getStatus()
        );
    }
}
