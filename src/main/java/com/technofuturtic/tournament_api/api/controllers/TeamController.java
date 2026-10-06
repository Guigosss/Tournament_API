package com.technofuturtic.tournament_api.api.controllers;

import com.technofuturtic.tournament_api.api.models.UserContext;
import com.technofuturtic.tournament_api.api.models.team.requests.TeamCreateRequest;
import com.technofuturtic.tournament_api.api.models.team.requests.TeamUpdateRequest;
import com.technofuturtic.tournament_api.api.models.team.responses.TeamResponse;
import com.technofuturtic.tournament_api.bll.services.TeamService;
import com.technofuturtic.tournament_api.dl.entities.TeamEntity;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/teams")
@RequiredArgsConstructor
public class TeamController {

    private final TeamService teamService;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<TeamResponse> create(
            @Valid @RequestBody TeamCreateRequest request,
            @AuthenticationPrincipal UserContext user
    ) {
        var team = teamService.create(
                request.name(),
                user.id()
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(TeamResponse.fromTeam(team));
    }

    @PutMapping("/{teamId}")
    @PreAuthorize("hasAuthority('admin') or @captainService.isTeamCaptain(#teamId)")
    public ResponseEntity<Void> update(
            @PathVariable Integer teamId,
            @Valid @RequestBody TeamUpdateRequest teamUpdateRequest
    ) {
        teamService.update(teamId, teamUpdateRequest);

        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{teamId}")
    @PreAuthorize("hasAuthority('admin') or @captainService.isTeamCaptain(#teamId)")
    public ResponseEntity<Void> delete(
            @PathVariable Integer teamId
    ) {
        teamService.delete(teamId);

        return ResponseEntity.noContent().build();
    }
}