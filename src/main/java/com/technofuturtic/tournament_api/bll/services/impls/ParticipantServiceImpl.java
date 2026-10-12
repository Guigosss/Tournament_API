package com.technofuturtic.tournament_api.bll.services.impls;

import com.technofuturtic.tournament_api.api.models.tournament.responses.ParticipantCountResponse;
import com.technofuturtic.tournament_api.api.models.tournament.responses.ParticipantResponse;
import com.technofuturtic.tournament_api.bll.exceptions.participant.ParticipantNotFoundException;
import com.technofuturtic.tournament_api.bll.exceptions.registration.RegistrationNotFoundException;
import com.technofuturtic.tournament_api.bll.exceptions.team.TeamNotFoundException;
import com.technofuturtic.tournament_api.bll.exceptions.tournament.TournamentNotFoundException;
import com.technofuturtic.tournament_api.bll.services.ParticipantService;
import com.technofuturtic.tournament_api.dal.repositories.*;
import com.technofuturtic.tournament_api.dl.entities.ParticipantEntity;
import com.technofuturtic.tournament_api.dl.entities.RegisterTeamEntity;
import com.technofuturtic.tournament_api.dl.entities.RegisterUserEntity;
import com.technofuturtic.tournament_api.dl.entities.TournamentEntity;
import com.technofuturtic.tournament_api.dl.enums.ParticipantType;
import com.technofuturtic.tournament_api.dl.enums.RegistrationStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ParticipantServiceImpl implements ParticipantService {

    private final ParticipantRepository participantRepository;
    private final TournamentRepository tournamentRepository;
    private final RegisterUserRepository registerUserRepository;
    private final RegisterTeamRepository registerTeamRepository;
    private final TeamRepository teamRepository;


    //Liste des participants avec informations pour un tournoi
    @Override
    @Transactional(readOnly = true)
    public List<ParticipantResponse> getByTournament(Integer tournamentId) {

        if (!tournamentRepository.existsById(tournamentId)) {
            throw new TournamentNotFoundException("Tournament non trouvé, Id : " + tournamentId);
        }

        return participantRepository.findByTournamentId(tournamentId).stream()
                .map(this::toResponse)
                .toList();
    }


    //Information sur un participant d'un tournoi
    @Override
    @Transactional(readOnly = true)
    public ParticipantResponse getById(Integer tournamentId, Integer participantId) {

        if (!tournamentRepository.existsById(tournamentId)) {
            throw new TournamentNotFoundException("Tournament non trouvé, Id : " + tournamentId);
        }

        ParticipantEntity participant = participantRepository
                .findByIdAndTournamentId(participantId, tournamentId)
                .orElseThrow(() -> new ParticipantNotFoundException("Participant non trouvé, Id : " + participantId));

        return toResponse(participant);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ParticipantResponse> getByTournamentIdAndTeamId(Integer tournamentId, Integer teamId) {
        if (!tournamentRepository.existsById(tournamentId)) {
           throw new TournamentNotFoundException("Tournament non trouvé, Id : " + tournamentId);
        }

       if(!teamRepository.existsById(teamId)) {
           throw new TeamNotFoundException("Team non trouvé, Id : " + teamId);
       }
        return participantRepository.findByTeamIdAndTournamentId(teamId, tournamentId).stream()
                .map(this::toResponse)
                .toList();
    }

    //Méthode interne à la classe, retrouve l'inscription liée au participant
    //pour récupérer la date et le statut
    private ParticipantResponse toResponse(ParticipantEntity participant) {
        Integer tournamentId = participant.getTournament().getId();

        if (participant.getUser() != null) {
            Integer userId = participant.getUser().getId();
            RegisterUserEntity registration = registerUserRepository
                    .findByUserIdAndTournamentId(userId, tournamentId)
                    .orElseThrow(() -> new RegistrationNotFoundException("Aucune inscription pour ce joueur dans ce tournoi, Id : " + userId));
            return ParticipantResponse.fromUser(participant, registration);
        }

        Integer teamId = participant.getTeam().getId();
        RegisterTeamEntity registration = registerTeamRepository
                .findByTeamIdAndTournamentId(teamId, tournamentId)
                .orElseThrow(() -> new RegistrationNotFoundException("Aucune inscription pour cette team dans ce tournoi, Id : " + teamId));
        return ParticipantResponse.fromTeam(participant, registration);
    }

    //Nombre de participants d'un tournament, maximum et places restantes
    @Override
    @Transactional(readOnly = true)
    public ParticipantCountResponse count(Integer tournamentId) {
        TournamentEntity tournament = tournamentRepository.findById(tournamentId)
                .orElseThrow(() -> new TournamentNotFoundException("Tournament non trouvé, Id : " + tournamentId));

        long current = (tournament.getParticipantType() == ParticipantType.PLAYER)
                ? registerUserRepository.countByTournamentIdAndStatus(tournamentId, RegistrationStatus.VALIDATED)
                : registerTeamRepository.countByTournamentIdAndStatus(tournamentId, RegistrationStatus.VALIDATED);

        int max = tournament.getMaxParticipants();

        return new ParticipantCountResponse(tournamentId, current, max, Math.max(0, max - current));
    }
}
