package com.technofuturtic.tournament_api.api.controllers;

import com.technofuturtic.tournament_api.bll.models.UserContext;
import com.technofuturtic.tournament_api.api.models.team.requests.TeamCreateRequest;
import com.technofuturtic.tournament_api.bll.models.team.requests.TeamUpdateRequest;
import com.technofuturtic.tournament_api.api.models.team.responses.TeamResponse;
import com.technofuturtic.tournament_api.api.models.team.responses.TeamSearchResponse;
import java.util.List;
import com.technofuturtic.tournament_api.bll.services.TeamService;
import com.technofuturtic.tournament_api.dl.entities.TeamEntity;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/teams")
@RequiredArgsConstructor
public class TeamController {

    private final TeamService teamService;

    @GetMapping("/search")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<TeamSearchResponse>> search(
            @RequestParam(required = false) String name
    ) {
        List<TeamSearchResponse> results = teamService.search(name)
                .stream()
                .map(team -> new TeamSearchResponse(
                        team.getId(),
                        team.getName(),
                        team.getNumberOfWins(),
                        team.getMembers().size(),
                        team.isArchived()
                ))
                .toList();

        return ResponseEntity.ok(results);
    }

    @GetMapping("/mine")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<TeamResponse>> findMine(
            @AuthenticationPrincipal UserContext user
    ) {
        List<TeamResponse> teams = teamService.findMine(user.id())
                .stream()
                .map(TeamResponse::fromTeam)
                .toList();

        return ResponseEntity.ok(teams);
    }

    @GetMapping("/{teamId}")
    public ResponseEntity<TeamResponse> findById(@PathVariable Integer teamId) {
        TeamEntity team = teamService.findById(teamId);
        return ResponseEntity.ok(TeamResponse.fromTeam(team));
    }

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