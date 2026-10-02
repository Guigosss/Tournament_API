package com.technofuturtic.tournament_api.api.controllers;

import com.technofuturtic.tournament_api.api.models.tournament.responses.PhaseResponse;
import com.technofuturtic.tournament_api.api.models.tournament.responses.TournamentBracketResponse;
import com.technofuturtic.tournament_api.bll.services.TournamentEngineService;
import com.technofuturtic.tournament_api.bll.services.TournamentGenerationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/tournaments")
public class TournamentController {

    private final TournamentEngineService tournamentEngineService;
    private final TournamentGenerationService tournamentGenerationService;

    @PostMapping("/{tournamentId}/generate")
    public ResponseEntity<Void> generateTournament(@PathVariable Integer tournamentId) {
        tournamentEngineService.generateTournament(tournamentId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{tournamentId}/bracket")
    public ResponseEntity<TournamentBracketResponse> getBracket(@PathVariable Integer tournamentId) {
        return ResponseEntity.ok(tournamentGenerationService.getBracket(tournamentId));
    }

    @GetMapping("/{tournamentId}/phases")
    public ResponseEntity<List<PhaseResponse>> getPhases(@PathVariable Integer tournamentId) {
        return ResponseEntity.ok(tournamentGenerationService.getPhases(tournamentId));
    }
}
