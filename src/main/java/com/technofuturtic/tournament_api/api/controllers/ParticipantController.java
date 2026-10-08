package com.technofuturtic.tournament_api.api.controllers;

import com.technofuturtic.tournament_api.api.models.tournament.responses.ParticipantCountResponse;
import com.technofuturtic.tournament_api.api.models.tournament.responses.ParticipantResponse;
import com.technofuturtic.tournament_api.bll.services.ParticipantService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/tournaments/{tournamentId}/participants")
public class ParticipantController {

    private final ParticipantService participantService;

    //TODO Définir qui peut voir l'email des participants (organisateur seulement ?)
    //Liste des participants d'un tournament
    @GetMapping
    public ResponseEntity<List<ParticipantResponse>> getByTournament(
            @PathVariable Integer tournamentId
    ) {
        return ResponseEntity.ok(participantService.getByTournament(tournamentId));
    }

    //Information d'un participant d'un tournament
    @GetMapping("/{participantId}")
    public ResponseEntity<ParticipantResponse> getById(
            @PathVariable Integer tournamentId,
            @PathVariable Integer participantId
    ) {
        return ResponseEntity.ok(participantService.getById(tournamentId, participantId));
    }

    @GetMapping("/teams/{teamId}")
    public ResponseEntity<List<ParticipantResponse>> getByTournamentIdAndTeamId(
            @PathVariable Integer tournamentId,
            @PathVariable Integer teamId
    ) {
        return ResponseEntity.ok(participantService.getByTournamentIdAndTeamId(tournamentId, teamId));
    }

    //Nombre de participants, maximum et places restantes
    @GetMapping("/count")
    public ResponseEntity<ParticipantCountResponse> count(@PathVariable Integer tournamentId) {
        return ResponseEntity.ok(participantService.count(tournamentId));
    }
}