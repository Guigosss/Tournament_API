package com.technofuturtic.tournament_api.api.controllers;

import com.technofuturtic.tournament_api.bll.models.UserContext;
import com.technofuturtic.tournament_api.bll.models.tournament.requests.MatchResultRequest;
import com.technofuturtic.tournament_api.bll.models.tournament.responses.MatchResponse;
import com.technofuturtic.tournament_api.bll.models.tournament.responses.UserMatchResponse;
import com.technofuturtic.tournament_api.bll.services.MatchService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

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

    @GetMapping("/me")
    public ResponseEntity<List<UserMatchResponse>> getMine(@AuthenticationPrincipal UserContext user) {
        return ResponseEntity.ok(matchService.getUserMatches(user.id()));
    }
}
