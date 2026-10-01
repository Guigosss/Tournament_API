package com.technofuturtic.tournament_api.bll.services.impls;

import com.technofuturtic.tournament_api.api.models.tournament.responses.RegistrationReponse;
import com.technofuturtic.tournament_api.bll.exceptions.team.TeamNotFoundException;
import com.technofuturtic.tournament_api.bll.exceptions.tournament.TournamentNotFoundException;
import com.technofuturtic.tournament_api.bll.exceptions.user.UserNotFoundException;
import com.technofuturtic.tournament_api.bll.services.RegistrationService;
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


    //Lier un user avec un tournament dans la table register_user
    @Override
    public RegistrationReponse registrationPlayer(Integer tournamentId, Integer userId) {
        TournamentEntity tournament = tournamentRepository.findById(tournamentId)
                .orElseThrow(() -> new TournamentNotFoundException("Tournament non trouvé, Id : " + tournamentId));

        checkRegistrationType(tournament, ParticipantType.PLAYER);

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User non trouvé, Id : " + userId));

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

    //Lier une team avec un tournament dans la table register_team
    @Override
    public RegistrationReponse registrationTeam(Integer tournamentId, Integer teamId) {
        TournamentEntity tournament = tournamentRepository.findById(tournamentId)
                .orElseThrow(() -> new TournamentNotFoundException("Tournament non trouvé, Id : " + tournamentId));

        checkRegistrationType(tournament, ParticipantType.TEAM);

        TeamEntity team = teamRepository.findById(teamId)
                .orElseThrow(() -> new TeamNotFoundException("Team non trouvé, Id : " + teamId));

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

    //Désinscrire un player d'un tournament
    @Override
    public void unregisterPlayer(Integer tournamentId, Integer userId) {
        TournamentEntity tournament = tournamentRepository.findById(tournamentId)
                .orElseThrow(() -> new TournamentNotFoundException("Tournament non trouvé, Id : " + tournamentId));

        checkRegistrationOpen(tournament);

        RegisterUserEntity registration = registerUserRepository
                .findByUserIdAndTournamentId(userId, tournamentId)
                .orElseThrow(() -> new UserNotFoundException("User non trouvé, Id : " + userId));

        if (registration.getStatus() == RegistrationStatus.EXCLUDED) {
            throw new IllegalStateException("Un joueur exclu ne peut pas annuler son inscription.");
        }

        registerUserRepository.delete(registration);

    }

    //Désinscrire une team d'un tournament
    @Override
    public void unregisterTeam(Integer tournamentId, Integer teamId) {
        TournamentEntity tournament = tournamentRepository.findById(tournamentId)
                .orElseThrow(() -> new TournamentNotFoundException("Tournament non trouvé, Id : " + tournamentId));

        checkRegistrationOpen(tournament);

        RegisterTeamEntity registration = registerTeamRepository
                .findByTeamIdAndTournamentId(teamId, tournamentId)
                .orElseThrow(() -> new TeamNotFoundException("Team non trouvé, Id : " + teamId));

        if (registration.getStatus() == RegistrationStatus.EXCLUDED) {
            throw new IllegalStateException("Une équipe exclue ne peut pas annuler son inscription.");
        }

        registerTeamRepository.delete(registration);

    }

    //Méthode interne à la classe, contrôle si la date pour s'inscrire est valide
    //et si le tournament est ouvert
    private void checkRegistrationOpen(TournamentEntity tournament) {
        if (tournament.getStatus() != TournamentStatus.REGISTRATION_OPEN) {
            throw new IllegalStateException("Les inscriptions ne sont pas ouvertes.");
        }

        LocalDate today = LocalDate.now();
        if (today.isBefore(tournament.getRegistrationStartDate())
                || today.isAfter(tournament.getRegistrationEndDate())) {
            throw new IllegalStateException("La période d'inscription est terminée ou n'a pas encore commencé.");
        }
    }

    //Méthode interne à la classe, contrôle lors de l'enregistrement si c'est bien
    //une team ou un player par rapport au tournament
    private void checkRegistrationType(TournamentEntity tournament, ParticipantType expectedType) {
        checkRegistrationOpen(tournament);

        if (tournament.getParticipantType() != expectedType) {
            throw new IllegalStateException("Ce tournoi n'accepte pas ce type de participant.");
        }
    }
}
