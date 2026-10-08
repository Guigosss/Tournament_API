package com.technofuturtic.tournament_api.api.controllers;

import com.technofuturtic.tournament_api.api.models.tournament.requests.OrganizerTransferRequest;
import com.technofuturtic.tournament_api.api.models.tournament.requests.TournamentUpdateRequest;
import com.technofuturtic.tournament_api.api.models.tournament.responses.PhaseResponse;
import com.technofuturtic.tournament_api.api.models.tournament.responses.TournamentBracketResponse;
import com.technofuturtic.tournament_api.api.models.tournament.responses.TournamentDetailResponse;
import com.technofuturtic.tournament_api.bll.services.TournamentEngineService;
import com.technofuturtic.tournament_api.bll.services.TournamentGenerationService;
import com.technofuturtic.tournament_api.api.models.tournament.requests.TournamentRequest;
import com.technofuturtic.tournament_api.api.models.tournament.responses.TournamentResponse;
import com.technofuturtic.tournament_api.bll.services.TournamentService;
import com.technofuturtic.tournament_api.dl.enums.TournamentStatus;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.DeleteMapping;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/tournaments")
public class TournamentController {

    private final TournamentService tournamentService;
    private final TournamentEngineService tournamentEngineService;
    private final TournamentGenerationService tournamentGenerationService;

    //Création d'un tournament
    @PostMapping
    public ResponseEntity<TournamentResponse> create(
            @Valid
            @RequestBody TournamentRequest request
    ) {
        TournamentResponse created = tournamentService.create(request);
        return ResponseEntity.ok(created);
    }

    //Information d'un tournament par Id, avec ses participants
    @GetMapping("/{id}")
    public ResponseEntity<TournamentDetailResponse> getById(
            @PathVariable Integer id
    ) {
        return ResponseEntity.ok(tournamentService.getDetail(id));
    }

    //Liste de tous les tournaments
    @GetMapping
    public ResponseEntity<List<TournamentResponse>> getAll(
            @RequestParam(required = false) TournamentStatus status
    ) {
        return ResponseEntity.ok(tournamentService.getAll(status));
    }

    //Mise à jour d'un tournament par Id
    @PreAuthorize("hasAuthority('admin') or @securityService.isTournamentOrganizer(#tournamentId)")
    @PutMapping({"/{tournamentId}"})
    public ResponseEntity<TournamentResponse> update(
            @PathVariable Integer tournamentId,
            @Valid
            @RequestBody TournamentUpdateRequest request
    ) {
        return ResponseEntity.ok(tournamentService.update(tournamentId, request));
    }

    //Met un tournament en statut CANCELED
    @PreAuthorize("hasAuthority('admin') or @securityService.isTournamentOrganizer(#tournamentId)")
    @DeleteMapping("/{tournamentId}")
    public ResponseEntity<TournamentResponse> cancel(
            @PathVariable Integer tournamentId
    ) {
        return ResponseEntity.ok(tournamentService.cancel(tournamentId));
    }

    //Ouvrir les inscriptions d'un tournament
    @PreAuthorize("hasAuthority('admin') or @securityService.isTournamentOrganizer(#tournamentId)")
    @PatchMapping("/{tournamentId}/open-registrations")
    public ResponseEntity<TournamentResponse> openRegistrations(@PathVariable Integer tournamentId) {
        return ResponseEntity.ok(tournamentService.changeStatus(tournamentId, TournamentStatus.REGISTRATION_OPEN));
    }

    //Fermer les inscriptions d'un tournament
    @PreAuthorize("hasAuthority('admin') or @securityService.isTournamentOrganizer(#tournamentId)")
    @PatchMapping("/{tournamentId}/close-registrations")
    public ResponseEntity<TournamentResponse> closeRegistrations(@PathVariable Integer tournamentId) {
        return ResponseEntity.ok(tournamentService.changeStatus(tournamentId, TournamentStatus.REGISTRATION_CLOSED));
    }

    @PreAuthorize("hasAuthority('admin') or @securityService.isTournamentOrganizer(#tournamentId)")
    @PostMapping("/{tournamentId}/generate")
    public ResponseEntity<Void> generateTournament(@PathVariable Integer tournamentId) {
        tournamentEngineService.generateTournament(tournamentId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{tournamentId}/bracket")
    public ResponseEntity<TournamentBracketResponse> getBracket(@PathVariable Integer tournamentId) {
        return ResponseEntity.ok(tournamentGenerationService.getBracket(tournamentId));
    }

    @GetMapping("/{tournamentId}/phases")
    public ResponseEntity<List<PhaseResponse>> getPhases(@PathVariable Integer tournamentId) {
        return ResponseEntity.ok(tournamentGenerationService.getPhases(tournamentId));
    }

    //TODO Vérifier que c'est bien l'organisateur actuel
    //Transférer l'organisation d'un tournament
    @PatchMapping("/{id}/organizer")
    public ResponseEntity<TournamentResponse> changeOrganizer(
            @PathVariable Integer id,
            @Valid
            @RequestBody OrganizerTransferRequest request
    ) {
        return ResponseEntity.ok(tournamentService.changeOrganizer(id, request.newOrganizerId()));
    }

}
