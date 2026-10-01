package com.technofuturtic.tournament_api.bll.services.impls;

import com.technofuturtic.tournament_api.api.models.tournament.requests.TournamentRequest;
import com.technofuturtic.tournament_api.api.models.tournament.responses.TournamentReponse;
import com.technofuturtic.tournament_api.bll.exceptions.tournament.TournamentNotFoundException;
import com.technofuturtic.tournament_api.bll.exceptions.user.UserNotFoundException;
import com.technofuturtic.tournament_api.bll.services.TournamentService;
import com.technofuturtic.tournament_api.dal.repositories.TournamentRepository;
import com.technofuturtic.tournament_api.dal.repositories.UserRepository;
import com.technofuturtic.tournament_api.dl.entities.TournamentEntity;
import com.technofuturtic.tournament_api.dl.entities.UserEntity;
import com.technofuturtic.tournament_api.dl.enums.TournamentStatus;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TournamentServiceImpl implements TournamentService {

    private final TournamentRepository tournamentRepository;
    private final UserRepository userRepository;


    //Création d'un tournament
    @Override
    @Transactional
    public TournamentReponse create(TournamentRequest request){
        validateDates(request);

        UserEntity organizer = userRepository.findById(request.organizerId())
                .orElseThrow(() -> new UserNotFoundException("Organisateur non trouvé, Id : " + request.organizerId()));

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

        return TournamentReponse.fromEntity(tournamentRepository.save(tournament));
    }

    //Vérification des dates
    @Override
    public void validateDates(TournamentRequest request) {
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
    public TournamentReponse getById(Integer id) {
        return tournamentRepository.findById(id)
                .map(TournamentReponse::fromEntity)
                .orElseThrow(() -> new TournamentNotFoundException("Tournament non trouvé, Id : " + id));
    }

    //Récupération d'une liste avec tous les tournament
    @Override
    public List<TournamentReponse> getAll(TournamentStatus status) {
        List<TournamentEntity> tournaments = (status == null)
                ? tournamentRepository.findAll()
                : tournamentRepository.findByStatus(status);

        return tournaments.stream()
                .map(TournamentReponse::fromEntity)
                .toList();
    }

    //Mise à jour d'un tournament
    @Override
    public TournamentReponse update(Integer id, TournamentRequest request) {
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

        return TournamentReponse.fromEntity(tournamentRepository.save(tournament));
    }

    //Mettre un tournament en statut CANCELED
    @Override
    public TournamentReponse cancel(Integer id) {
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
        return TournamentReponse.fromEntity(tournamentRepository.save(tournament));
    }


}
