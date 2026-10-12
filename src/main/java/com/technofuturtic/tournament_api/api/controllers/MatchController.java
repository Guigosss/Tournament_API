package com.technofuturtic.tournament_api.api.controllers;

import com.technofuturtic.tournament_api.api.models.tournament.requests.MatchResultRequest;
import com.technofuturtic.tournament_api.api.models.tournament.responses.MatchResponse;
import com.technofuturtic.tournament_api.bll.services.MatchService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

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
