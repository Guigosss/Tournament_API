package com.technofuturtic.tournament_api.bll.services.impls;

import com.technofuturtic.tournament_api.api.models.tournament.responses.RegistrationReponse;
import com.technofuturtic.tournament_api.bll.services.RegistrationService;
import com.technofuturtic.tournament_api.bll.services.TournamentService;
import com.technofuturtic.tournament_api.dal.repositories.*;
import com.technofuturtic.tournament_api.dl.entities.*;
import com.technofuturtic.tournament_api.dl.enums.ParticipantType;
import com.technofuturtic.tournament_api.dl.enums.RegistrationStatus;
import com.technofuturtic.tournament_api.dl.enums.TournamentStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class RegistrationServiceImpl implements RegistrationService {

    private final TournamentRepository tournamentRepository;
    private final UserRepository userRepository;
    private final TeamRepository teamRepository;
    private final RegisterUserRepository registerUserRepository;
    private final RegisterTeamRepository registerTeamRepository;


    @Override
    public RegistrationReponse registrationPlayer(Integer tournamentId, Integer userId) {
        TournamentEntity tournament = tournamentRepository.findById(tournamentId)
                .orElseThrow();

        checkRegistrationOpen(tournament, ParticipantType.PLAYER);

        UserEntity user = userRepository.findById(userId)
                .orElseThrow();

        if (registerUserRepository.existsByUserIdAndTournamentId(userId, tournamentId)) {
            throw new IllegalStateException("Joueur déjà inscrit.");
        }

        long taken = registerUserRepository
                .countByTournamentIdAndStatusNot(tournamentId, RegistrationStatus.EXCLUDED);
        if (taken >= tournament.getMaxParticipants()) {
            throw new IllegalStateException("Le tournoi est complet.");
        }

        RegisterUserEntity registration =  new RegisterUserEntity();
        registration.setUser(user);
        registration.setTournament(tournament);
        registration.setRegisterDate(LocalDateTime.now());
        registration.setStatus(RegistrationStatus.PENDING);

        return RegistrationReponse.fromUserRegistration(registerUserRepository.save(registration));
    }

    @Override
    public RegistrationReponse registrationTeam(Integer tournamentId, Integer teamId) {
        TournamentEntity tournament = tournamentRepository.findById(tournamentId)
                .orElseThrow();

        checkRegistrationOpen(tournament, ParticipantType.TEAM);

        TeamEntity team = teamRepository.findById(teamId)
                .orElseThrow();

        if (registerTeamRepository.existsByTeamIdAndTournamentId(teamId, tournamentId)) {
            throw new IllegalStateException("Équipe déjà inscrite.");
        }

        if (team.getMembers().size() < team.getTeamSize()){
            throw new IllegalStateException("L'équipe ne compte pas assez de membres.");
        }

        Long taken = registerTeamRepository
                .countByTournamentIdAndStatusNot(tournamentId, RegistrationStatus.EXCLUDED);
        if (taken >= tournament.getMaxParticipants()) {
            throw new IllegalStateException("Le tournoi est complet.");
        }

        RegisterTeamEntity registration =  new RegisterTeamEntity();
        registration.setTeam(team);
        registration.setTournament(tournament);
        registration.setRegisterDate(LocalDateTime.now());
        registration.setStatus(RegistrationStatus.PENDING);

        return RegistrationReponse.fromTeamRegistration(registerTeamRepository.save(registration));
    }


    private void checkRegistrationOpen(TournamentEntity tournament, ParticipantType participantType) {
        if (tournament.getStatus() != TournamentStatus.REGISTRATION_OPEN) {
            throw new IllegalStateException("Les inscriptions ne sont pas ouverte.");
        }

        LocalDate today = LocalDate.now();
        if (today.isBefore(tournament.getRegistrationStartDate())
        || today.isAfter(tournament.getRegistrationEndDate())) {
            throw new IllegalStateException("La période d'inscription est terminée ou n'a pas encore commencé.");
        }

        if (tournament.getParticipantType() != participantType) {
            throw new IllegalStateException("Ce tournoi n'accepte pas ce type de participant.");
        }
    }
}
