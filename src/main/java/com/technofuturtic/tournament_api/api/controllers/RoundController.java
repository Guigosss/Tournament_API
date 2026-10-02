package com.technofuturtic.tournament_api.api.controllers;

import com.technofuturtic.tournament_api.api.models.tournament.responses.MatchResponse;
import com.technofuturtic.tournament_api.bll.services.TournamentGenerationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/rounds")
@RequiredArgsConstructor
public class RoundController {

    private final TournamentGenerationService tournamentGenerationService;

    @GetMapping("/{tournamentId}/phases/{phaseId}/rounds/{roundId}/matches")
    public ResponseEntity<List<MatchResponse>> getMatches(@PathVariable Integer tournamentId,
                                                          @PathVariable Integer phaseId,
                                                          @PathVariable Integer roundId) {
        return ResponseEntity.ok(tournamentGenerationService.getMatches(tournamentId, phaseId, roundId));
    }
}
