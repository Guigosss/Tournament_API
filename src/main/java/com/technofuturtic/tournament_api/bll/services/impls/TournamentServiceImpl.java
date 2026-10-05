package com.technofuturtic.tournament_api.bll.services.impls;

import com.technofuturtic.tournament_api.api.models.UserContext;
import com.technofuturtic.tournament_api.api.models.tournament.requests.TournamentRequest;
import com.technofuturtic.tournament_api.api.models.tournament.responses.TournamentResponse;
import com.technofuturtic.tournament_api.bll.exceptions.tournament.TournamentNotFoundException;
import com.technofuturtic.tournament_api.bll.exceptions.user.UserNotFoundException;
import com.technofuturtic.tournament_api.bll.services.TournamentService;
import com.technofuturtic.tournament_api.dal.repositories.TournamentRepository;
import com.technofuturtic.tournament_api.dal.repositories.UserRepository;
import com.technofuturtic.tournament_api.dl.entities.TournamentEntity;
import com.technofuturtic.tournament_api.dl.entities.UserEntity;
import com.technofuturtic.tournament_api.dl.enums.TournamentStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TournamentServiceImpl implements TournamentService {

    private final TournamentRepository tournamentRepository;
    private final UserRepository userRepository;


    //Création d'un tournament
    @Override
    @Transactional
    public TournamentResponse create(TournamentRequest request) {
        validateDates(request);

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        UserContext userContext = (UserContext) authentication.getPrincipal();

        UserEntity organizer = userRepository.findById(userContext.id())
                .orElseThrow(() ->
                        new UserNotFoundException(
                                "Utilisateur connecté non trouvé : " + userContext.id()
                        )
                );


        TournamentEntity tournament = new TournamentEntity();
        tournament.setName(request.name());
        tournament.setDescription(request.description());
        tournament.setMaxParticipants(request.maxParticipants());
        tournament.setFormat(request.format());
        tournament.setParticipantType(request.participantType());
        tournament.setStartDate(request.startDate());
        tournament.setEndDate(request.endDate());
        tournament.setRegistrationStartDate(request.registrationStartDate());
        tournament.setRegistrationEndDate(request.registrationEndDate());
        tournament.setOrganizer(organizer);
        tournament.setStatus(TournamentStatus.UPCOMING);

        return TournamentResponse.fromEntity(
                tournamentRepository.save(tournament)
        );
    }

    //Vérification des dates
    private void validateDates(TournamentRequest request) {
        if (request.endDate().isBefore(request.startDate())) {
            throw new IllegalArgumentException("La date de fin doit être après la date de début");
        }
        if (request.registrationEndDate().isBefore(request.registrationStartDate())) {
            throw new IllegalArgumentException("La fin des inscriptions doit être après leur début");
        }
        if (request.registrationEndDate().isAfter(request.startDate())) {
            throw new IllegalArgumentException("Les inscriptions doivent se terminer avant le début du tournoi");
        }

    }

    //Récupération d'un tournament avec l'ID
    @Override
    @Transactional(readOnly = true)
    public TournamentResponse getById(Integer id) {
        return tournamentRepository.findById(id)
                .map(TournamentResponse::fromEntity)
                .orElseThrow(() -> new TournamentNotFoundException("Tournament non trouvé, Id : " + id));
    }

    //Récupération d'une liste avec tous les tournament
    @Override
    @Transactional(readOnly = true)
    public List<TournamentResponse> getAll(TournamentStatus status) {
        List<TournamentEntity> tournaments = (status == null)
                ? tournamentRepository.findAll()
                : tournamentRepository.findByStatus(status);

        return tournaments.stream()
                .map(TournamentResponse::fromEntity)
                .toList();
    }

    //Mise à jour d'un tournament
    @Override
    @Transactional
    public TournamentResponse update(Integer id, TournamentRequest request) {
        TournamentEntity tournament = tournamentRepository.findById(id)
                .orElseThrow(() -> new TournamentNotFoundException("Tournament non trouvé, Id : " + id));

        TournamentStatus status = tournament.getStatus();
        if (status == TournamentStatus.IN_PROGRESS
                || status == TournamentStatus.FINISHED
                || status == TournamentStatus.CANCELED) {
            throw new IllegalStateException("Le tournoi ne peut plus être modifié.");
        }

        validateDates(request);

        tournament.setName(request.name());
        tournament.setDescription(request.description());
        tournament.setMaxParticipants(request.maxParticipants());
        tournament.setFormat(request.format());
        tournament.setParticipantType(request.participantType());
        tournament.setStartDate(request.startDate());
        tournament.setEndDate(request.endDate());
        tournament.setRegistrationStartDate(request.registrationStartDate());
        tournament.setRegistrationEndDate(request.registrationEndDate());

        return TournamentResponse.fromEntity(tournamentRepository.save(tournament));
    }

    //Mettre un tournament en statut CANCELED
    @Override
    @Transactional
    public TournamentResponse cancel(Integer id) {
        TournamentEntity tournament = tournamentRepository.findById(id)
                .orElseThrow(() -> new TournamentNotFoundException("Tournament non trouvé, Id : " + id));

        TournamentStatus status = tournament.getStatus();
        if (status == TournamentStatus.IN_PROGRESS
                || status == TournamentStatus.FINISHED){
            throw new IllegalStateException("Le tournoi a déjà commencé ; il ne peut pas être annulé.");
        }
        if (status == TournamentStatus.CANCELED) {
            throw new IllegalStateException("Le tournoi est déjà annulé.");
        }

        tournament.setStatus(TournamentStatus.CANCELED);
        return TournamentResponse.fromEntity(tournamentRepository.save(tournament));
    }

    //Changer le statut d'un tournament en respectant les transitions autorisées
    @Override
    @Transactional
    public TournamentResponse changeStatus(Integer id, TournamentStatus newStatus) {
        TournamentEntity tournament = tournamentRepository.findById(id)
                .orElseThrow(() -> new TournamentNotFoundException("Tournament non trouvé, Id : " + id));

        if (!tournament.getStatus().canTransitionTo(newStatus)) {
            throw new IllegalStateException("Passage de " + tournament.getStatus()
                    + " à " + newStatus + " impossible.");
        }

        tournament.setStatus(newStatus);
        return TournamentResponse.fromEntity(tournamentRepository.save(tournament));
    }

}
