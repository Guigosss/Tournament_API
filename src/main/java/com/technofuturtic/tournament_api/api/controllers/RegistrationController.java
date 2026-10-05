package com.technofuturtic.tournament_api.api.controllers;

import com.technofuturtic.tournament_api.api.models.tournament.requests.PlayerRegistrationRequest;
import com.technofuturtic.tournament_api.api.models.tournament.requests.TeamRegistrationRequest;
import com.technofuturtic.tournament_api.api.models.tournament.responses.RegistrationResponse;
import com.technofuturtic.tournament_api.bll.services.RegistrationService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;

@RestController
@RequestMapping("/tournaments/{tournamentId}/registrations")
@AllArgsConstructor
public class RegistrationController {

    private final RegistrationService registrationService;

    //Lier un player avec un tournament
    @PostMapping("/players")
    public ResponseEntity<RegistrationResponse> registerPlayer(
            @PathVariable Integer tournamentId,
            @Valid
            @RequestBody PlayerRegistrationRequest request
            ) {
        return ResponseEntity.ok(registrationService.registrationPlayer(tournamentId, request.userId()));
    }

    //Lier une team avec un tournament
    @PostMapping("/teams")
    public ResponseEntity<RegistrationResponse> registerTeam(
            @PathVariable Integer tournamentId,
            @Valid
            @RequestBody TeamRegistrationRequest request
    ) {
        return ResponseEntity.ok(registrationService.registrationTeam(tournamentId, request.teamId()));
    }

    //Désinscrire un player d'un tournament
    @DeleteMapping("/players/{userId}")
    public ResponseEntity<Void> unregisterPlayer(
            @PathVariable Integer tournamentId,
            @PathVariable Integer userId
    ) {
        registrationService.unregisterPlayer(tournamentId, userId);
        return ResponseEntity.ok().build();
    }

    //Désinscrire une team d'un tournament
    @DeleteMapping("/teams/{teamId}")
    public ResponseEntity<Void> unregisterTeam(
            @PathVariable Integer tournamentId,
            @PathVariable Integer teamId
    ) {
        registrationService.unregisterTeam(tournamentId, teamId);
        return ResponseEntity.ok().build();
    }

    //TODO Vérifier que c'est bien l'organisateur
    //Valider l'inscription d'un player
    @PatchMapping("/players/{userId}/validate")
    public ResponseEntity<RegistrationResponse> validatePlayer(
            @PathVariable Integer tournamentId,
            @PathVariable Integer userId
    ) {
        return ResponseEntity.ok(registrationService.validatePlayer(tournamentId, userId));
    }

    //TODO Vérifier que c'est bien l'organisateur
    //Exclure un player
    @PatchMapping("/players/{userId}/exclude")
    public ResponseEntity<RegistrationResponse> excludePlayer(
            @PathVariable Integer tournamentId,
            @PathVariable Integer userId
    ) {
        return ResponseEntity.ok(registrationService.excludePlayer(tournamentId, userId));
    }

    //TODO Vérifier que c'est bien l'organisateur
    //Valider l'inscription d'une team
    @PatchMapping("/teams/{teamId}/validate")
    public ResponseEntity<RegistrationResponse> validateTeam(
            @PathVariable Integer tournamentId,
            @PathVariable Integer teamId
    ) {
        return ResponseEntity.ok(registrationService.validateTeam(tournamentId, teamId));
    }

    //TODO Vérifier que c'est bien l'organisateur
    //Exclure une team
    @PatchMapping("/teams/{teamId}/exclude")
    public ResponseEntity<RegistrationResponse> excludeTeam(
            @PathVariable Integer tournamentId,
            @PathVariable Integer teamId
    ) {
        return ResponseEntity.ok(registrationService.excludeTeam(tournamentId, teamId));
    }
}
