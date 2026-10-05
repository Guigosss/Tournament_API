package com.technofuturtic.tournament_api.api.controllers;

import com.technofuturtic.tournament_api.api.models.UserContext;
import com.technofuturtic.tournament_api.api.models.team.requests.CreateTeamRequest;
import com.technofuturtic.tournament_api.api.models.team.responses.TeamResponse;
import com.technofuturtic.tournament_api.bll.services.TeamService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/teams")
@RequiredArgsConstructor
public class TeamController {
    private final TeamService teamService;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<TeamResponse> create(@Valid @RequestBody CreateTeamRequest request,
                                               @AuthenticationPrincipal UserContext user) {
        var team = teamService.create(request.name(), user.id());
        return ResponseEntity.status(HttpStatus.CREATED).body(TeamResponse.fromTeam(team));
    }
}