package com.technofuturtic.tournament_api.api.controllers;

import com.technofuturtic.tournament_api.api.models.tournament.requests.MatchResultRequest;
import com.technofuturtic.tournament_api.api.models.tournament.responses.MatchResponse;
import com.technofuturtic.tournament_api.bll.services.MatchService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/matches")
@RequiredArgsConstructor
public class MatchController {

    private final MatchService matchService;

    @PatchMapping("/{matchId}/result")
    public ResponseEntity<MatchResponse> submitResult(@PathVariable Integer matchId,
                                                      @RequestBody MatchResultRequest request) {
        return ResponseEntity.ok(matchService.submitResult(matchId, request));
    }
}
