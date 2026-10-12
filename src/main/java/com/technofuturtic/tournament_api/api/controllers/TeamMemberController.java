package com.technofuturtic.tournament_api.api.controllers;

import com.technofuturtic.tournament_api.api.models.UserContext;
import com.technofuturtic.tournament_api.bll.services.TeamMemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/teams/{teamId}/members")
@RequiredArgsConstructor
public class TeamMemberController {
    private final TeamMemberService memberService;

    @DeleteMapping("/{playerId}")
    @PreAuthorize("hasAuthority('admin') or @captainService.isTeamCaptain(#teamId)")
    public ResponseEntity<Void> remove(@PathVariable Integer teamId, @PathVariable Integer playerId,
                                       @AuthenticationPrincipal UserContext user) {
        memberService.remove(teamId, playerId, user.id(), "admin".equals(user.role()));
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> leave(@PathVariable Integer teamId, @AuthenticationPrincipal UserContext user) {
        memberService.leave(teamId, user.id());
        return ResponseEntity.noContent().build();
    }
}