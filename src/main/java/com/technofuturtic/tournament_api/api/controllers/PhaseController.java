package com.technofuturtic.tournament_api.api.controllers;

import com.technofuturtic.tournament_api.api.models.tournament.responses.RoundResponse;
import com.technofuturtic.tournament_api.bll.services.TournamentGenerationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/phases")
@RequiredArgsConstructor
public class PhaseController {

    private final TournamentGenerationService tournamentGenerationService;

    @GetMapping("/{tournamentId}/phases/{phaseId}/rounds")
    public ResponseEntity<List<RoundResponse>> getRounds(@PathVariable Integer tournamentId,
                                                         @PathVariable Integer phaseId) {
        return ResponseEntity.ok(tournamentGenerationService.getRounds(tournamentId, phaseId));
    }
}
