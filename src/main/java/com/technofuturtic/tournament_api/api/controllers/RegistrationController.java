package com.technofuturtic.tournament_api.api.controllers;

import com.technofuturtic.tournament_api.api.models.tournament.requests.PlayerRegistrationRequest;
import com.technofuturtic.tournament_api.api.models.tournament.requests.TeamRegistrationRequest;
import com.technofuturtic.tournament_api.api.models.tournament.responses.RegistrationReponse;
import com.technofuturtic.tournament_api.bll.services.RegistrationService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/tournaments/{tournamentId}/registrations")
@AllArgsConstructor
public class RegistrationController {

    private final RegistrationService registrationService;

    //Lier un player avec un tournament
    @PostMapping("/players")
    public ResponseEntity<RegistrationReponse> registerPlayer(
            @PathVariable Integer tournamentId,
            @Valid
            @RequestBody PlayerRegistrationRequest request
            ) {
        return ResponseEntity.ok(registrationService.registrationPlayer(tournamentId, request.userId()));
    }

    //Lier une team avec un tournament
    @PostMapping("/teams")
    public ResponseEntity<RegistrationReponse> registerTeam(
            @PathVariable Integer tournamentId,
            @Valid
            @RequestBody TeamRegistrationRequest request
    ) {
        return ResponseEntity.ok(registrationService.registrationTeam(tournamentId, request.teamId()));
    }

    //Désinscrire un player d'un tournament
    @DeleteMapping("/players/{userId}")
    public ResponseEntity<Void> unregisterPlayer(@PathVariable Integer tournamentId,
                                                 @PathVariable Integer userId) {
        registrationService.unregisterPlayer(tournamentId, userId);
        return ResponseEntity.ok().build();
    }

    //Désinscrire une team d'un tournament
    @DeleteMapping("/teams/{teamId}")
    public ResponseEntity<Void> unregisterTeam(@PathVariable Integer tournamentId,
                                               @PathVariable Integer teamId) {
        registrationService.unregisterTeam(tournamentId, teamId);
        return ResponseEntity.ok().build();
    }
}
