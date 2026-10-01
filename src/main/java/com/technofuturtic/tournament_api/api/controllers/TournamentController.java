package com.technofuturtic.tournament_api.api.controllers;

import com.technofuturtic.tournament_api.api.models.tournament.requests.TournamentRequest;
import com.technofuturtic.tournament_api.api.models.tournament.responses.TournamentReponse;
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
    public ResponseEntity<TournamentReponse> create(
            @Valid
            @RequestBody TournamentRequest request
    ) {
        TournamentReponse created = tournamentService.create(request);
        return ResponseEntity.ok(created);
    }

    //Information d'un tournament par Id
    @GetMapping("/{id}")
    public ResponseEntity<TournamentReponse> getById(
            @PathVariable Integer id
    ) {
        return ResponseEntity.ok(tournamentService.getById(id));
    }

    //Liste de tous les tournaments
    @GetMapping
    public ResponseEntity<List<TournamentReponse>> getAll(
            @RequestParam(required = false) TournamentStatus status
    ) {
        return ResponseEntity.ok(tournamentService.getAll(status));
    }

    //Mise à jour d'un tournament par Id
    @PutMapping({"/{id}"})
    public ResponseEntity<TournamentReponse> update(
            @PathVariable Integer id,
            @Valid
            @RequestBody TournamentRequest request
    ) {
        return ResponseEntity.ok(tournamentService.update(id, request));
    }

    //Met un tournament en statut CANCELED
    @DeleteMapping("/{id}")
    public ResponseEntity<TournamentReponse> cancel(
            @PathVariable Integer id
    ) {
        return ResponseEntity.ok(tournamentService.cancel(id));
    }

}
