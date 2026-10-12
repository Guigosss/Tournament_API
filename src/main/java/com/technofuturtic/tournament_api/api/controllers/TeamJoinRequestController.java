package com.technofuturtic.tournament_api.api.controllers;

import com.technofuturtic.tournament_api.api.models.UserContext;
import com.technofuturtic.tournament_api.api.models.team.responses.TeamJoinRequestResponse;
import com.technofuturtic.tournament_api.bll.services.TeamJoinRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class TeamJoinRequestController {
    private final TeamJoinRequestService service;

    @PostMapping("/teams/{teamId}/join-requests")
    public ResponseEntity<TeamJoinRequestResponse> submit(@PathVariable Integer teamId, @AuthenticationPrincipal UserContext user) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.submit(teamId, user.id()));
    }

    @GetMapping("/join-requests")
    public List<TeamJoinRequestResponse> mine(@AuthenticationPrincipal UserContext user) {
        return service.findMine(user.id());
    }

    @GetMapping("/teams/{teamId}/join-requests")
    public List<TeamJoinRequestResponse> forTeam(@PathVariable Integer teamId, @AuthenticationPrincipal UserContext user) {
        return service.findForTeam(teamId, user.id(), "admin".equals(user.role()));
    }

    @PostMapping("/join-requests/{requestId}/accept")
    public TeamJoinRequestResponse accept(@PathVariable Integer requestId, @AuthenticationPrincipal UserContext user) {
        return service.accept(requestId, user.id(), "admin".equals(user.role()));
    }

    @PostMapping("/join-requests/{requestId}/reject")
    public TeamJoinRequestResponse reject(@PathVariable Integer requestId, @AuthenticationPrincipal UserContext user) {
        return service.reject(requestId, user.id(), "admin".equals(user.role()));
    }

    @PostMapping("/join-requests/{requestId}/cancel")
    public TeamJoinRequestResponse cancel(@PathVariable Integer requestId, @AuthenticationPrincipal UserContext user) {
        return service.cancel(requestId, user.id());
    }
}