package com.technofuturtic.tournament_api.api.controllers;

import com.technofuturtic.tournament_api.api.models.UserContext;
import com.technofuturtic.tournament_api.api.models.team.requests.TeamInvitationRequest;
import com.technofuturtic.tournament_api.api.models.team.responses.TeamInvitationResponse;
import com.technofuturtic.tournament_api.bll.services.TeamInvitationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class TeamInvitationController {
    private final TeamInvitationService invitationService;

    @PostMapping("/teams/{teamId}/invitations")
    @PreAuthorize("hasAuthority('admin') or @captainService.isTeamCaptain(#teamId)")
    public ResponseEntity<TeamInvitationResponse> invite(@PathVariable Integer teamId,
                                                         @Valid @RequestBody TeamInvitationRequest request, @AuthenticationPrincipal UserContext user) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
                invitationService.invite(teamId, request.playerId(), user.id(), "admin".equals(user.role())));
    }

    @GetMapping("/invitations")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<TeamInvitationResponse>> findMine(@AuthenticationPrincipal UserContext user) {
        return ResponseEntity.ok(invitationService.findMyInvitations(user.id()));
    }

    @PostMapping("/invitations/{invitationId}/accept")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<TeamInvitationResponse> accept(@PathVariable Integer invitationId,
                                                         @AuthenticationPrincipal UserContext user) {
        return ResponseEntity.ok(invitationService.accept(invitationId, user.id()));
    }

    @PostMapping("/invitations/{invitationId}/reject")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<TeamInvitationResponse> reject(@PathVariable Integer invitationId,
                                                         @AuthenticationPrincipal UserContext user) {
        return ResponseEntity.ok(invitationService.reject(invitationId, user.id()));
    }

    @PostMapping("/invitations/{invitationId}/cancel")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<TeamInvitationResponse> cancel(@PathVariable Integer invitationId,
                                                         @AuthenticationPrincipal UserContext user) {
        return ResponseEntity.ok(invitationService.cancel(invitationId, user.id(), "admin".equals(user.role())));
    }
}