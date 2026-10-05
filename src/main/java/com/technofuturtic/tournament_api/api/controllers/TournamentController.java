package com.technofuturtic.tournament_api.api.controllers;

import com.technofuturtic.tournament_api.api.models.tournament.requests.TournamentRequest;
import com.technofuturtic.tournament_api.api.models.tournament.responses.TournamentResponse;
import com.technofuturtic.tournament_api.bll.services.TournamentService;
import com.technofuturtic.tournament_api.dl.enums.TournamentStatus;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/tournaments")
public class TournamentController {

    private final TournamentService tournamentService;

    //Création d'un tournament
    @PostMapping
    public ResponseEntity<TournamentResponse> create(
            @Valid
            @RequestBody TournamentRequest request
    ) {
        TournamentResponse created = tournamentService.create(request);
        return ResponseEntity.ok(created);
    }

    //Information d'un tournament par Id
    @GetMapping("/{id}")
    public ResponseEntity<TournamentResponse> getById(
            @PathVariable Integer id
    ) {
        return ResponseEntity.ok(tournamentService.getById(id));
    }

    //Liste de tous les tournaments
    @GetMapping
    public ResponseEntity<List<TournamentResponse>> getAll(
            @RequestParam(required = false) TournamentStatus status
    ) {
        return ResponseEntity.ok(tournamentService.getAll(status));
    }

    //Mise à jour d'un tournament par Id
    @PutMapping({"/{id}"})
    public ResponseEntity<TournamentResponse> update(
            @PathVariable Integer id,
            @Valid
            @RequestBody TournamentRequest request
    ) {
        return ResponseEntity.ok(tournamentService.update(id, request));
    }

    //TODO Vérifier que c'est bien l'organisateur qui CANCELED
    //Met un tournament en statut CANCELED
    @DeleteMapping("/{id}")
    public ResponseEntity<TournamentResponse> cancel(
            @PathVariable Integer id
    ) {
        return ResponseEntity.ok(tournamentService.cancel(id));
    }

    //TODO Vérifier que c'est bien l'organisateur
    //Ouvrir les inscriptions d'un tournament
    @PatchMapping("/{id}/open-registrations")
    public ResponseEntity<TournamentResponse> openRegistrations(@PathVariable Integer id) {
        return ResponseEntity.ok(tournamentService.changeStatus(id, TournamentStatus.REGISTRATION_OPEN));
    }

    //TODO Vérifier que c'est bien l'organisateur
    //Fermer les inscriptions d'un tournament
    @PatchMapping("/{id}/close-registrations")
    public ResponseEntity<TournamentResponse> closeRegistrations(@PathVariable Integer id) {
        return ResponseEntity.ok(tournamentService.changeStatus(id, TournamentStatus.REGISTRATION_CLOSED));
    }

}
