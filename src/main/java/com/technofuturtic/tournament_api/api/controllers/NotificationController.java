package com.technofuturtic.tournament_api.api.controllers;

import com.technofuturtic.tournament_api.api.models.UserContext;
import com.technofuturtic.tournament_api.api.models.notification.NotificationResponse;
import com.technofuturtic.tournament_api.bll.services.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/notifications")
@PreAuthorize("isAuthenticated()")
@RequiredArgsConstructor
public class NotificationController {
    private final NotificationService notificationService;

    @GetMapping
    public ResponseEntity<List<NotificationResponse>> findMine(@AuthenticationPrincipal UserContext user) {
        return ResponseEntity.ok(notificationService.findMine(user.id()));
    }

    @PatchMapping("/{notificationId}/read")
    public ResponseEntity<NotificationResponse> markRead(@PathVariable Integer notificationId,
                                                         @AuthenticationPrincipal UserContext user) {
        return ResponseEntity.ok(notificationService.markRead(notificationId, user.id()));
    }
}