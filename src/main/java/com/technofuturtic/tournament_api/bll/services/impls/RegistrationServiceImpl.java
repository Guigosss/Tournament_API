package com.technofuturtic.tournament_api.bll.services.impls;

import com.technofuturtic.tournament_api.api.models.tournament.responses.RegistrationResponse;
import com.technofuturtic.tournament_api.bll.exceptions.registration.RegistrationNotFoundException;
import com.technofuturtic.tournament_api.bll.exceptions.team.TeamNotFoundException;
import com.technofuturtic.tournament_api.bll.exceptions.tournament.TournamentNotFoundException;
import com.technofuturtic.tournament_api.bll.exceptions.user.UserNotFoundException;
import com.technofuturtic.tournament_api.bll.services.RegistrationService;
import com.technofuturtic.tournament_api.dal.repositories.TournamentRepository;
import com.technofuturtic.tournament_api.dal.repositories.UserRepository;
import com.technofuturtic.tournament_api.dal.repositories.TeamRepository;
import com.technofuturtic.tournament_api.dal.repositories.RegisterTeamRepository;
import com.technofuturtic.tournament_api.dal.repositories.RegisterUserRepository;
import com.technofuturtic.tournament_api.dal.repositories.ParticipantRepository;
import com.technofuturtic.tournament_api.dl.entities.TournamentEntity;
import com.technofuturtic.tournament_api.dl.entities.UserEntity;
import com.technofuturtic.tournament_api.dl.entities.RegisterTeamEntity;
import com.technofuturtic.tournament_api.dl.entities.RegisterUserEntity;
import com.technofuturtic.tournament_api.dl.entities.TeamEntity;
import com.technofuturtic.tournament_api.dl.entities.ParticipantEntity;
import com.technofuturtic.tournament_api.dl.enums.ParticipantType;
import com.technofuturtic.tournament_api.dl.enums.RegistrationStatus;
import com.technofuturtic.tournament_api.dl.enums.TournamentStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private final ParticipantRepository participantRepository;


    //Lier un user avec un tournament dans la table register_user
    @Override
    @Transactional
    public RegistrationResponse registrationPlayer(Integer tournamentId, Integer userId) {
        TournamentEntity tournament = tournamentRepository.findById(tournamentId)
                .orElseThrow(() -> new TournamentNotFoundException("Tournament non trouvé, Id : " + tournamentId));

        checkRegistrationType(tournament, ParticipantType.PLAYER);

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User non trouvé, Id : " + userId));

        checkRegistrationOpen(tournament);

        if (registerUserRepository.existsByUserIdAndTournamentId(userId, tournamentId)) {
            throw new IllegalStateException("Joueur déjà inscrit.");
        }

        long taken = registerUserRepository
                .countByTournamentIdAndStatus(tournamentId, RegistrationStatus.VALIDATED);
        if (taken >= tournament.getMaxParticipants()) {
            throw new IllegalStateException("Le tournoi est complet.");
        }

        RegisterUserEntity registration =  new RegisterUserEntity();
        registration.setUser(user);
        registration.setTournament(tournament);
        registration.setRegisterDate(LocalDateTime.now());
        registration.setStatus(RegistrationStatus.PENDING);

        return RegistrationResponse.fromUserRegistration(registerUserRepository.save(registration));
    }

    //Lier une team avec un tournament dans la table register_team
    @Override
    @Transactional
    public RegistrationResponse registrationTeam(Integer tournamentId, Integer teamId) {
        TournamentEntity tournament = tournamentRepository.findById(tournamentId)
                .orElseThrow(() -> new TournamentNotFoundException("Tournament non trouvé, Id : " + tournamentId));

        checkRegistrationType(tournament, ParticipantType.TEAM);

        TeamEntity team = teamRepository.findById(teamId)
                .orElseThrow(() -> new TeamNotFoundException("Team non trouvé, Id : " + teamId));

        checkRegistrationOpen(tournament);

        if (registerTeamRepository.existsByTeamIdAndTournamentId(teamId, tournamentId)) {
            throw new IllegalStateException("Équipe déjà inscrite.");
        }

        if (team.getMembers().size() < team.getTeamSize()){
            throw new IllegalStateException("L'équipe ne compte pas assez de membres.");
        }

        Long taken = registerTeamRepository
                .countByTournamentIdAndStatus(tournamentId, RegistrationStatus.VALIDATED);
        if (taken >= tournament.getMaxParticipants()) {
            throw new IllegalStateException("Le tournoi est complet.");
        }

        RegisterTeamEntity registration =  new RegisterTeamEntity();
        registration.setTeam(team);
        registration.setTournament(tournament);
        registration.setRegisterDate(LocalDateTime.now());
        registration.setStatus(RegistrationStatus.PENDING);

        return RegistrationResponse.fromTeamRegistration(registerTeamRepository.save(registration));
    }

    //Désinscrire un player d'un tournament
    @Override
    @Transactional
    public void unregisterPlayer(Integer tournamentId, Integer userId) {
        TournamentEntity tournament = tournamentRepository.findById(tournamentId)
                .orElseThrow(() -> new TournamentNotFoundException("Tournament non trouvé, Id : " + tournamentId));

        checkRegistrationOpen(tournament);

        RegisterUserEntity registration = registerUserRepository
                .findByUserIdAndTournamentId(userId, tournamentId)
                .orElseThrow(() -> new RegistrationNotFoundException("Aucune inscription pour ce joueur dans ce tournoi, Id : " + userId));

        if (registration.getStatus() == RegistrationStatus.EXCLUDED) {
            throw new IllegalStateException("Un joueur exclu ne peut pas annuler son inscription.");
        }

        participantRepository
                .findByUserIdAndTournamentId(userId, tournamentId)
                .ifPresent(participantRepository::delete);

        registerUserRepository.delete(registration);

    }

    //Désinscrire une team d'un tournament
    @Override
    @Transactional
    public void unregisterTeam(Integer tournamentId, Integer teamId) {
        TournamentEntity tournament = tournamentRepository.findById(tournamentId)
                .orElseThrow(() -> new TournamentNotFoundException("Tournament non trouvé, Id : " + tournamentId));

        checkRegistrationOpen(tournament);

        RegisterTeamEntity registration = registerTeamRepository
                .findByTeamIdAndTournamentId(teamId, tournamentId)
                .orElseThrow(() -> new RegistrationNotFoundException("Aucune inscription pour cette team dans ce tournoi, Id : " + teamId));

        if (registration.getStatus() == RegistrationStatus.EXCLUDED) {
            throw new IllegalStateException("Une équipe exclue ne peut pas annuler son inscription.");
        }

        participantRepository
                .findByTeamIdAndTournamentId(teamId, tournamentId)
                .ifPresent(participantRepository::delete);

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

        if (tournament.getParticipantType() != expectedType) {
            throw new IllegalStateException("Ce tournoi n'accepte pas ce type de participant.");
        }
    }

    //Méthode interne à la classe, contrôle que les inscriptions peuvent encore être gérées
    //(validation, exclusion) : uniquement tant que le tournament n'a pas commencé
    private void checkRegistrationsManageable(TournamentEntity tournament) {
        TournamentStatus status = tournament.getStatus();
        if (status != TournamentStatus.REGISTRATION_OPEN
                && status != TournamentStatus.REGISTRATION_CLOSED) {
            throw new IllegalStateException("Les inscriptions ne peuvent plus être gérées pour ce tournoi.");
        }
    }

    //Valider un joueur
    @Override
    @Transactional
    public RegistrationResponse validatePlayer(Integer tournamentId, Integer userId) {
        TournamentEntity tournament = tournamentRepository.findById(tournamentId)
                .orElseThrow(() -> new TournamentNotFoundException("Tournament non trouvé, Id : " + tournamentId));

        checkRegistrationsManageable(tournament);

        RegisterUserEntity registration = registerUserRepository
                .findByUserIdAndTournamentId(userId, tournamentId)
                .orElseThrow(() -> new RegistrationNotFoundException("Aucune inscription pour ce joueur dans ce tournoi, Id : " + userId));

        if (registration.getStatus() != RegistrationStatus.PENDING) {
            throw new IllegalStateException("Seule une inscription en attente peut être validée.");
        }

        long validated = registerUserRepository
                .countByTournamentIdAndStatus(tournamentId, RegistrationStatus.VALIDATED);
        if (validated >= tournament.getMaxParticipants()) {
            throw new IllegalStateException("Le tournoi est complet.");
        }

        registration.setStatus(RegistrationStatus.VALIDATED);

        ParticipantEntity participant = new ParticipantEntity();
        participant.setTournament(tournament);
        participant.setUser(registration.getUser());
        participantRepository.save(participant);

        return RegistrationResponse.fromUserRegistration(registerUserRepository.save(registration));
    }

    //Valider une team
    @Override
    @Transactional
    public RegistrationResponse validateTeam(Integer tournamentId, Integer teamId) {
        TournamentEntity tournament = tournamentRepository.findById(tournamentId)
                .orElseThrow(() -> new TournamentNotFoundException("Tournament non trouvé, Id : " + tournamentId));

        checkRegistrationsManageable(tournament);

        RegisterTeamEntity registration = registerTeamRepository
                .findByTeamIdAndTournamentId(teamId, tournamentId)
                .orElseThrow(() -> new RegistrationNotFoundException("Aucune inscription pour cette team dans ce tournoi, Id : " + teamId));

        if (registration.getStatus() != RegistrationStatus.PENDING) {
            throw new IllegalStateException("Seule une inscription en attente peut être validée.");
        }

        long validated = registerTeamRepository
                .countByTournamentIdAndStatus(tournamentId, RegistrationStatus.VALIDATED);
        if (validated >= tournament.getMaxParticipants()) {
            throw new IllegalStateException("Le tournoi est complet.");
        }

        registration.setStatus(RegistrationStatus.VALIDATED);

        ParticipantEntity participant = new ParticipantEntity();
        participant.setTournament(tournament);
        participant.setTeam(registration.getTeam());
        participantRepository.save(participant);

        return RegistrationResponse.fromTeamRegistration(registerTeamRepository.save(registration));
    }

    //Exclure un player : son inscription passe en EXCLUDED et il n'est plus participant
    @Override
    @Transactional
    public RegistrationResponse excludePlayer(Integer tournamentId, Integer userId) {
        TournamentEntity tournament = tournamentRepository.findById(tournamentId)
                .orElseThrow(() -> new TournamentNotFoundException("Tournament non trouvé, Id : " + tournamentId));

        checkRegistrationsManageable(tournament);

        RegisterUserEntity registration = registerUserRepository
                .findByUserIdAndTournamentId(userId, tournamentId)
                .orElseThrow(() -> new RegistrationNotFoundException("Aucune inscription pour ce joueur dans ce tournoi, Id : " + userId));

        if (registration.getStatus() == RegistrationStatus.EXCLUDED) {
            throw new IllegalStateException("Ce joueur est déjà exclu.");
        }

        participantRepository
                .findByUserIdAndTournamentId(userId, tournamentId)
                .ifPresent(participantRepository::delete);

        registration.setStatus(RegistrationStatus.EXCLUDED);

        return RegistrationResponse.fromUserRegistration(registerUserRepository.save(registration));
    }

    //Exclure une team : son inscription passe en EXCLUDED et il n'est plus participant
    @Override
    @Transactional
    public RegistrationResponse excludeTeam(Integer tournamentId, Integer teamId) {

        TournamentEntity tournament = tournamentRepository.findById(tournamentId)
                .orElseThrow(() -> new TournamentNotFoundException("Tournament non trouvé, Id : " + tournamentId));

        checkRegistrationsManageable(tournament);

        RegisterTeamEntity registration = registerTeamRepository
                .findByTeamIdAndTournamentId(teamId, tournamentId)
                .orElseThrow(() -> new RegistrationNotFoundException("Aucune inscription pour cette team dans ce tournoi, Id : " + teamId));

        if (registration.getStatus() == RegistrationStatus.EXCLUDED) {
            throw new IllegalStateException("Cette team est déjà exclu.");
        }

        participantRepository
                .findByTeamIdAndTournamentId(teamId, tournamentId)
                .ifPresent(participantRepository::delete);

        registration.setStatus(RegistrationStatus.EXCLUDED);

        return RegistrationResponse.fromTeamRegistration(registerTeamRepository.save(registration));
    }
}
