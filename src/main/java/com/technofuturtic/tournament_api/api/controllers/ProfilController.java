package com.technofuturtic.tournament_api.api.controllers;

import org.springframework.security.access.prepost.PreAuthorize;
import com.technofuturtic.tournament_api.api.models.profil.requests.ProfilRequest;
import com.technofuturtic.tournament_api.api.models.profil.responses.ProfilResponse;
import com.technofuturtic.tournament_api.bll.services.ProfilService;
import com.technofuturtic.tournament_api.dl.entities.UserEntity;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.DeleteMapping;

@RestController
@RequiredArgsConstructor
public class ProfilController {

    private final ProfilService profilService;

    @GetMapping("/{id}")
    public ResponseEntity<ProfilResponse> findById(
            @PathVariable Integer id
    ) {
        UserEntity user = profilService.findById(id);

        ProfilResponse profilResponse =
                ProfilResponse.fromUserEntity(user);

        return ResponseEntity.ok(profilResponse);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('admin') or principal.id() == #id")
    public ResponseEntity<Void> update(
            @PathVariable Integer id,
            @Valid @RequestBody ProfilRequest profilRequest
    ) {
        UserEntity user = profilRequest.toUserEntity();

        profilService.update(id, user);

        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('admin') or principal.id() == #id")
    public ResponseEntity<Void> delete(
            @PathVariable Integer id
    ) {
        profilService.delete(id);

        return ResponseEntity.noContent().build();
    }
}